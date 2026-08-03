# Evaluation plan

## Metrics
We propose to evaluate algorithms using a range of metrics found in the literature

### Mean asbsolute cents error

### Gross pitch error GPE
Proportion of major errors (+/- 20 cents)

## Data sets
### ChoraleBricks
Source: https://zenodo.org/records/15081741

Description: Woodwind data with f0 labels. Used in the real time SWIPE paper

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

### Small Saw
Real recordings of saw, labelled by note. Labels are quite rough so will need to be manually corrected or used for more qualitative measurement

### Personal recordings
Do some stuff. Subjective labelling.


## Methodology
Generate CSV data in kotlin
Use jupyter notebook for analysis