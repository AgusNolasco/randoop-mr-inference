#Usage: ./experiments/run.sh {subject_set} {subject_name} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops}

. experiments/init_env.sh

subject_set=$1
subject_name=$2
gen_strategy=$3
mrs_to_fuzz=$4
allow_epa_loops=$5

export MRS_DIR="$EPA_INFERENCE_DIR/output"

export SUBJECT_DIR=/tmp/$(echo $subject_name | sed 's/_//')b

export SUBJECT_NAME=$subject_name

subject_cp="$SUBJECT_DIR/target/commons-cli-1.1-SNAPSHOT.jar"

input_file="experiments/$subject_set-subjects/$subject_name.properties"

mkdir -p "output/$subject_name/allow_epa_loops_$allow_epa_loops/$gen_strategy/$mrs_to_fuzz/"

# TODO: When compute mutation we must have the original class somewhere, and use it here, to ensure
# that we are running over the original class instead of a mutant
# orig_class_path=$(find $SUBJECTS_DIR/$subject_name/src/main/java/ -type f -name "$subject_name.java")
# cp $SUBJECTS_DIR/$subject_name/mutants/original-class/$subject_name.java $orig_class_path

CURR_DIR=$PWD

cd "$SUBJECT_DIR/"
defects4j compile > /dev/null
cd $CURR_DIR

echo "Running $subject_name"
/usr/bin/time -f"%e" java -cp "$subject_cp:$RANDOOP_DIR/randoop-all-4.3.1.jar" -Xbootclasspath/a:${RANDOOP_DIR}/replacecall-4.3.1.jar -javaagent:${RANDOOP_DIR}/replacecall-4.3.1.jar randoop.main.Main gentests --classlist=$input_file --output-limit=2000 --time-limit=0 --literals-level=ALL --literals-file=literals/lits.txt --deterministic=true --forbid-null=true --gen-strategy=$gen_strategy --mrs-to-fuzz=$mrs_to_fuzz --allow-epa-loops=$allow_epa_loops > output/$subject_name/allow_epa_loops_$allow_epa_loops/$gen_strategy/$mrs_to_fuzz/log.txt
