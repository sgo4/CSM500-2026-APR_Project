import numpy as np
import pandas as pd
from dataclasses import dataclass
from pathlib import Path



class PitchMetrics:
    """
    Functionality for measuring pitch accuracy
    """

    @dataclass
    class PitchMetricsResult:
        """
        Data class to hold results
        """
        gpe: float
        mae: float



    """
    Generates metrics for two arrays of pitch estimates

    MAE (mean absolute error)
    GPE (Gross pitch error)
    """
    def evaluate(self, estimates, reference) -> PitchMetricsResult:
        mae = self.mean_absolute_pitch_error(estimates,reference)
        gpe = self.gross_pitch_error(estimates, reference)
        return self.PitchMetricsResult(gpe,mae)

    """
    Standard validation for comparing two pitch arrays
    """
    def validate_pitch_arrays(self, estimates, reference):
        if np.any(estimates <= 0) or np.any(reference <= 0):
            raise ValueError("Frequencies must be positive")
        elif estimates.shape != reference.shape:
            raise ValueError("Estimate / reference arrays must have the same shape")

    """
    Calculate cents difference for 2 arrays of non negative pitch estimates
    against a given reference
    """
    def cents_difference(self, estimates, reference):
        self.validate_pitch_arrays(estimates, reference)
        
        return 1200 * np.log2(estimates/reference)

    """
    Calculate mean absolute pitch error for two arrays of non negative pitch estimates
    against a given reference
    """
    def mean_absolute_pitch_error(self, estimates, reference):
        return np.mean(
            np.abs(
                self.cents_difference(estimates,reference)
            )
        )

    """
    Calculate gross pitch error (GPE) for two arrays of non negative pitch estimates
    against a given reference
    """
    def gross_pitch_error(self, estimates, reference, threshold_cents=50):
        self.validate_pitch_arrays(estimates, reference)

        cents_error = self.cents_difference(estimates,reference)
        gross_error = np.abs(cents_error) > threshold_cents
        return 100 * gross_error.mean()


class PitchExperimentData:

    def __init__(self, path_to_csv: Path):
        assert str(path_to_csv).endswith(".csv")
        self.df = pd.read_csv(path_to_csv,sep=";")
        self.results = None

    def head(self):
        return self.df.head()

    def align_timestamps(self, labelled_data: pd.DataFrame, experimental_data: pd.DataFrame):
        """
        Align timestamps for labelled f0 data + experimental data
        Map each timestamp in the labelled data to the closest in the experimental data
        Experimental data assumed to be considerably more granular than labelled data
        """

        experimental_data_renamed = experimental_data.rename(columns={"timestamp": "timestamp_estimator","f0":"measured_f0"})

        result = pd.merge_asof(
            labelled_data,
            experimental_data_renamed[["timestamp_estimator", "measured_f0"]],
            left_on="t",
            right_on="timestamp_estimator",
            direction="nearest",
            tolerance=None,
        )
        return result

    def find_labelled_data(self, file, df) -> Path:
        """
        Find labelled f0 data for a given track
        """
        track, fn = file.split("__")
        dataset_name = df["dataset"].iat[0]
        data_location = Path.cwd().parent / "resources" / "datasets" / dataset_name / track / "annotations" / (fn.split(".")[0] + "_f0_filled.csv")
        return data_location

    def process_dataset(self, dataset, df) -> list:
        """
        Process a single data set
        """
        
        dataset_res = [] # container for results
        metrics = PitchMetrics() # metrics utility

        # filter df by dataset
        dataset_df = df[df["dataset"] == dataset]

        # process results for each file (pre-processing ensures unique names)
        for file in dataset_df["file"].unique():
            labels = pd.read_csv(self.find_labelled_data(file,df))
            file_df = df[df["file"] == file]
            estimators = file_df["estimator"].unique()
            for estimator in estimators:
                estimator_df = file_df[file_df["estimator"] == estimator] # a dataframe containing the results of a single estimator on a single file
                processing_time = estimator_df["processing_time_file"].mean()
                time_aligns = self.align_timestamps(labels,estimator_df) # align in time
                time_aligns_voiced = time_aligns[(time_aligns["f0"] > 0) & (time_aligns["measured_f0"] > 0)]
                reference_freq, estimated_freq = time_aligns_voiced["f0"], time_aligns_voiced["measured_f0"]
                estimator_res = metrics.evaluate(estimated_freq, reference_freq)

                # append results as a tuple of dataset, file, estimator, gpe, mae
                dataset_res.append([dataset,file,estimator,estimator_res.mae,estimator_res.gpe,processing_time])
        return dataset_res

    def process(self) -> pd.DataFrame:
        """
        Process experimental results as a dataset
        """        
        experiments_df = self.df
        datasets = experiments_df["dataset"].unique()

        # store results as tuples (dataset,file,estimator, metric( GPE, MAE etc.))
        res = [
            ["dataset","track","estimator","MAE","GPE","processing_time_file"]
        ]

        for dataset in datasets:
            dataset_results = self.process_dataset(dataset,experiments_df)
            res += dataset_results

        res = pd.DataFrame(res[1:],columns=res[0])
        # add a column specifically for instrument

        res["instrument"] = (
            res["track"]
                .str
                .split("_")
                .str[-1]
                .str
                .removesuffix(".wav")
        )
        self.results = res # cach results for analysis

        return res

    def summary(self, reprocess=False, df=None):
        """
        Return a summary data frame showing results per estimator per dataset
        """
        if df is not None:
            res = df
        elif reprocess or self.results is None:
            res = self.process()

        else:
            res = self.results

        summary = (
            res.groupby(["estimator", "dataset"], as_index=False)
            .agg({
                "MAE": "mean",
                "GPE": "mean",
                "processing_time_file": "mean"
            })
        )
        return pd.DataFrame(summary)

        
        