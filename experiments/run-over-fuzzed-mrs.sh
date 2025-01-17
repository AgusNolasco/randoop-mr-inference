#Usage: ./experiments/run.sh {subject_set} {subject_name} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops}

. experiments/init_env.sh

subject_set=$1
subject_name=$2
mrs_to_fuzz=$3
allow_epa_loops=$4
seed=$5

export MRS_DIR="$EPA_INFERENCE_DIR/output"

export SUBJECT_NAME=$subject_name

subject_cp="$SUBJECTS_DIR/$subject_name/build/libs/*"

input_file="experiments/$subject_set-subjects/$subject_name.properties"
omit_methods_file="$EPA_INFERENCE_DIR/output/$subject_name/methods-to-ignore.txt"

mkdir -p "output/$subject_name/allow_epa_loops_$allow_epa_loops/EPA_CONFORMING/$mrs_to_fuzz/$seed/"

echo "Running $subject_name"
java -cp "$subject_cp:$RANDOOP_DIR/randoop-all-4.3.1.jar" -Xbootclasspath/a:${RANDOOP_DIR}/replacecall-4.3.1.jar -javaagent:${RANDOOP_DIR}/replacecall-4.3.1.jar randoop.main.Main gentests --classlist=$input_file --omit-methods-file=$omit_methods_file --output-limit=2000 --time-limit=0 --literals-level=ALL --literals-file=literals/lits.txt --randomseed=$seed --deterministic=true --forbid-null=true --gen-strategy=EPA_CONFORMING --mrs-to-fuzz=$mrs_to_fuzz --allow-epa-loops=$allow_epa_loops --run-over-fuzzed-mrs > output/$subject_name/allow_epa_loops_$allow_epa_loops/EPA_CONFORMING/$mrs_to_fuzz/$seed/run-over-fuzzed-mrs-log.txt
