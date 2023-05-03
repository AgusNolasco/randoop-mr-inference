#Usage: ./experiments/run.sh {subject_set} {subject_name} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops}

. experiments/init_env.sh

subject_set=$1
subject_name=$2
gen_strategy=$3
mrs_to_fuzz=$4
allow_epa_loops=$5

export MRS_DIR="$EPA_INFERENCE_DIR/output"

export SUBJECT_DIR=/home/investigador/nolasco/d4j-data/$(echo $subject_name | sed 's/_//')b

export SUBJECT_NAME=$subject_name

project_name=${subject_name%%_*}
if [ $project_name = Codec ]; then
	subject_cp="$SUBJECT_DIR/dist/*"
elif [ $subject_name = Gson_9 ]; then
	subject_cp="$SUBJECT_DIR/gson/target/*"
else
	subject_cp="$SUBJECT_DIR/target/*"
fi

input_file="experiments/$subject_set-subjects/$subject_name.properties"
# omit_methods_file="$EPA_INFERENCE_DIR/output/$subject_name/methods-to-ignore.txt"

mkdir -p "output/$subject_name/allow_epa_loops_$allow_epa_loops/$gen_strategy/$mrs_to_fuzz/random/"

echo "Running $subject_name"
/usr/bin/time -f"%e" java -cp "$subject_cp:$RANDOOP_DIR/randoop-all-4.3.1.jar" -Xbootclasspath/a:${RANDOOP_DIR}/replacecall-4.3.1.jar -javaagent:${RANDOOP_DIR}/replacecall-4.3.1.jar randoop.main.Main gentests --classlist=$input_file --output-limit=2000 --time-limit=0 --literals-level=ALL --literals-file=literals/lits.txt --deterministic=true --forbid-null=true --gen-strategy=$gen_strategy --mrs-to-fuzz=$mrs_to_fuzz --allow-epa-loops=$allow_epa_loops --random > output/$subject_name/allow_epa_loops_$allow_epa_loops/$gen_strategy/$mrs_to_fuzz/random/log.txt
