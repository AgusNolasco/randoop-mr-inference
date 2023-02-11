#Usage: ./experiments/run.sh {subject_set} {subject_name} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops} {mutant_number} {REDUCED|INFERRED}

source experiments/init_env.sh

subject_set=$1
subject_name=$2
gen_strategy=$3
mrs_to_fuzz=$4
allow_epa_loops=$5
mutant_number=$6
over=$7

if [ $over == "REDUCED" ]; then
    export MRS_DIR="$ALLOY_DIR/output"
elif [ $over == "INFERRED" ]; then
    export MRS_DIR="output"
elif [ $over == "RANDOM" ]; then
    export MRS_DIR="output"
else
    echo "Unexpected MRs dir"
    exit 1
fi

subject_cp="$SUBJECTS_DIR/$subject_name/build/libs/*"

input_file="experiments/$subject_set-subjects/$subject_name.properties"
omit_methods_file="$EPA_INFERENCE_DIR/output/$subject_name/methods-to-ignore.txt"

mkdir -p "output/$subject_name/allow_epa_loops_$allow_epa_loops/$gen_strategy/$mrs_to_fuzz/mutants/$mutant_number/$over/"

mkdir -p "$SUBJECTS_DIR/$subject_name/mutants/original-class/"

orig_class_path=$(find $SUBJECTS_DIR/$subject_name/src/main/java/ -type f -name "$subject_name.java")
mutated_class_path=$(find $SUBJECTS_DIR/$subject_name/mutants/$mutant_number/ -type f -name "$subject_name.java")

cp $orig_class_path $SUBJECTS_DIR/$subject_name/mutants/original-class/$subject_name.java

cp $mutated_class_path $orig_class_path

CURR_DIR=$PWD

cd "$SUBJECTS_DIR/$subject_name/"
./gradlew jar
mutant_compiles=$?
cd $CURR_DIR

#--omit-methods-file=$omit_methods_file 
if [ $mutant_compiles -eq 0 ]; then
    if [ $over == "RANDOM" ]; then
        java -cp "$subject_cp:$RANDOOP_DIR/randoop-all-4.3.1.jar" -Xbootclasspath/a:${RANDOOP_DIR}/replacecall-4.3.1.jar -javaagent:${RANDOOP_DIR}/replacecall-4.3.1.jar randoop.main.Main gentests --classlist=$input_file --output-limit=2000 --time-limit=0 --literals-level=ALL --literals-file=literals/lits.txt --deterministic=true --forbid-null=true --gen-strategy=$gen_strategy --mrs-to-fuzz=$mrs_to_fuzz --allow-epa-loops=$allow_epa_loops --run-over-mutant --random > output/$subject_name/allow_epa_loops_$allow_epa_loops/$gen_strategy/$mrs_to_fuzz/mutants/$mutant_number/$over/log.txt
    else
        java -cp "$subject_cp:$RANDOOP_DIR/randoop-all-4.3.1.jar" -Xbootclasspath/a:${RANDOOP_DIR}/replacecall-4.3.1.jar -javaagent:${RANDOOP_DIR}/replacecall-4.3.1.jar randoop.main.Main gentests --classlist=$input_file --output-limit=2000 --time-limit=0 --literals-level=ALL --literals-file=literals/lits.txt --deterministic=true --forbid-null=true --gen-strategy=$gen_strategy --mrs-to-fuzz=$mrs_to_fuzz --allow-epa-loops=$allow_epa_loops --run-over-mutant > output/$subject_name/allow_epa_loops_$allow_epa_loops/$gen_strategy/$mrs_to_fuzz/mutants/$mutant_number/$over/log.txt
    fi
else
    echo "The mutant does not compile" > output/$subject_name/allow_epa_loops_$allow    _epa_loops/$gen_strategy/$mrs_to_fuzz/mutants/$mutant_number/$over/log.txt
fi

cp $SUBJECTS_DIR/$subject_name/mutants/original-class/$subject_name.java $orig_class_path

cd "$SUBJECTS_DIR/$subject_name/"
./gradlew jar
cd $CURR_DIR
