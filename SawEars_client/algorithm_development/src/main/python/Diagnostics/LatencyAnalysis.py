class FrameResult:

    def __init__(
            self,
            frameId,
            total,
            estimation_started,
            estimation_complete,
            animation_state_emitted,
            curve_drawing_started,
            curve_drawing_completed
            ):
        self.frameId = frameId
        self.total = total
        self.estimation_started = estimation_started
        self.estimation_complete = estimation_complete
        self.animation_state_emitted = animation_state_emitted
        self.curve_drawing_started = curve_drawing_started
        self.curve_drawing_completed = curve_drawing_completed

class AnalysisResult:
    def __init__(self,
        n_frames,
        total,
        estimation_started,
        estimation_complete,
        animation_state_emitted,
        curve_drawing_started,
        curve_drawing_completed
        ):
        self.n_frames = n_frames
        self.total = total
        self.estimation_started = estimation_started
        self.estimation_complete = estimation_complete
        self.animation_state_emitted = animation_state_emitted
        self.curve_drawing_started = curve_drawing_started
        self.curve_drawing_completed = curve_drawing_completed

    def __str__(self):
        return f"""
    Avg. time per frame: {self.total:.2f} ms
    Avg. time to start estimation: {self.estimation_started:.2f} ms
    Avg. time to complete estimation: {self.estimation_complete:.2f} ms
    Avg. time to emit animation state: {self.animation_state_emitted:.2f} ms
    Avg. time to start curve drawing: {self.curve_drawing_started:.2f} ms
    Avg. time to complete curve drawing: {self.curve_drawing_completed:.2f} ms
    Frames processed: {self.n_frames}"""

class ResultsAnalyser:

    def __init__(self):
        self.results = {
            "count": 0,
            "total": 0,
            "estimation_started": 0,
            "estimation_complete": 0,
            "animation_state_emitted": 0,
            "curve_drawing_started": 0,
            "curve_drawing_completed": 0
        }

    def add_frame(self, frame: FrameResult):

        self.results["count"] += 1
        self.results["total"] += frame.total
        self.results["estimation_started"] += frame.estimation_started
        self.results["estimation_complete"] += frame.estimation_complete
        self.results["animation_state_emitted"] += frame.animation_state_emitted
        self.results["curve_drawing_started"] += frame.curve_drawing_started
        self.results["curve_drawing_completed"] += frame.curve_drawing_completed

    def analyse(self) -> AnalysisResult|None:
        res = self.results

    
        n_samples = res["count"]
        if n_samples < 1:
            return None
        return AnalysisResult(
            n_samples,
            res["total"] / n_samples,
            res["estimation_started"] / n_samples,
            res["estimation_complete"] / n_samples,
            res["animation_state_emitted"] / n_samples,
            res["curve_drawing_started"] / n_samples,
            res["curve_drawing_completed"] / n_samples,
        )



def main():

    disregarded_frames = 0
    fn = "latencyDiagnostics_mcleod.csv"

    acc = {}
    with open(fn) as fin:
        i = 1
        for line in fin:
            if i == 1:
                i+=1
                continue
            if line.strip():
                frameId, stage, ts = line.split(",")
                process_line(frameId, stage, int(ts.strip()), acc)

    analyser = ResultsAnalyser()
    for frameId in acc:

        frameResult = analyse_frame(frameId, acc[frameId])
        if frameResult is not None:
            analyser.add_frame(frameResult)
        else:
            disregarded_frames += 1

    analysis = analyser.analyse()
    print(analysis)
    if disregarded_frames > 0:
        print(f"    Frames discarded: {disregarded_frames}")

    
        



def process_line(frameId, stage, timeStampNanos, accumulator: dict):
    """Read csv line into accumulator, grouping frame marks together"""
    if not frameId in accumulator:
        accumulator[frameId] = {}

    accumulator[frameId][stage] = timeStampNanos


def analyse_frame(frameId, frame: dict) -> (FrameResult|None):


    try: 
        fr = frame["FRAME_READ"]
        es = frame["ESTIMATION_STARTED"]
        ec = frame["ESTIMATION_COMPLETE"]
        se = frame["ANIMATION_STATE_EMITTED"]
        ds = frame["CURVE_DRAWING_STARTED"]
        dc = frame["CURVE_DRAWING_COMPLETED"]

        # diregard frames where stages are recorded out of order
        if not (fr <= es <= ec <= se <= ds <= dc):
            print("out of whack")
            offset = frame["FRAME_READ"]
            for f in frame:
                print(f, (frame[f] - offset) / 1000)

            exit()
            return None
    except KeyError:
        return None

    
    total = (dc-fr) / 1_000_000 # convert nanos difference to millis
    t1 = (es - fr) / 1_000_000 
    t2 = (ec - es) / 1_000_000 
    t3 = (se - ec) / 1_000_000 
    t4 = (ds - se) / 1_000_000
    t5 = (dc - ds) / 1_000_000

    return FrameResult(
            frameId,
            total,
            t1,
            t2,
            t3,
            t4,
            t5
    )
    

if __name__ == "__main__":
    main()
