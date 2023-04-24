#Usage: ./experiments/run.sh {subject_set} {subject_name} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops}

. experiments/init_env.sh

subject_set=$1
subject_name=$2
mutant_number=$3

export MUTANT_NUM=$mutant_number

subject_cp="$SUBJECTS_DIR/$subject_name/build/libs/*"

input_file="experiments/$subject_set-subjects/$subject_name.properties"

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

if [ $mutant_compiles -eq 0 ]; then
    java -cp "$subject_cp:$RANDOOP_DIR/randoop-all-4.3.1.jar" -Xbootclasspath/a:${RANDOOP_DIR}/replacecall-4.3.1.jar -javaagent:${RANDOOP_DIR}/replacecall-4.3.1.jar randoop.main.Main gentests --classlist=$input_file --output-limit=2000 --time-limit=0 --literals-level=ALL --literals-file=literals/lits.txt --deterministic=true --forbid-null=true --SBES
else
    mkdir -p "output/$subject_name/sbes-mutation/$mutant_number/"
    echo "Mutant $mutant_number killed? : 1" > "output/$subject_name/sbes-mutation/$mutant_number/SBES-mutant-results.txt"
fi

cp $SUBJECTS_DIR/$subject_name/mutants/original-class/$subject_name.java $orig_class_path

cd "$SUBJECTS_DIR/$subject_name/"
./gradlew jar
cd $CURR_DIR
