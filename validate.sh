subject_set=$1
subject=$2
mrs_fuzzed=$3

experiments/run.sh $subject_set $subject EPA_AWARE $mrs_fuzzed true
