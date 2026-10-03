# Evaluation plan

## Metrics
We propose to evaluate algorithms using a range of metrics found in the literature

### Mean asbsolute cents error
Average absolute error across all windows in cents

### Gross pitch error GPE
Proportion of major errors (+/- 50 cents)

## Data sets
### ChoraleBricks
Source: https://zenodo.org/records/15081741

Description: Woodwind and brass data with f0 labels. Used in the real time SWIPE paper.

#### File structure

##### /tracks
Tracks per recording per instrument.

as = alto sax
bar = baritone
cl = clarinet
fl = flute
ob = oboe
tp = tuba
fh = french horn


## Methodology
Generate CSV data in kotlin
Use jupyter notebook for analysis