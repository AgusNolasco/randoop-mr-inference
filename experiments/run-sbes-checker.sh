#Usage: ./experiments/run.sh {subject_set} {subject_name} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops}

source experiments/init_env.sh

subject_set=$1
subject_name=$2
mutant_number=$3

export MUTANT_NUM=$mutant_number

subject_cp="$SUBJECTS_DIR/$subject_name/build/libs/*"

input_file="experiments/$subject_set-subjects/$subject_name.properties"

java -cp "$subject_cp:$RANDOOP_DIR/randoop-all-4.3.1.jar" -Xbootclasspath/a:${RANDOOP_DIR}/replacecall-4.3.1.jar -javaagent:${RANDOOP_DIR}/replacecall-4.3.1.jar randoop.main.Main gentests --classlist=$input_file --output-limit=2000 --time-limit=0 --literals-level=ALL --literals-file=literals/lits.txt --deterministic=true --forbid-null=true --SBES
