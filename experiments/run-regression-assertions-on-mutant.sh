#Usage: ./experiments/run.sh {subject_set} {subject_name} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops}

. experiments/init_env.sh

subject_set=$1
subject_name=$2
mutant_number=$3

export MUTANT_NUM=$mutant_number

subject_cp="$SUBJECTS_DIR/$subject_name/build/libs/*"

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

mkdir -p "output/$subject_name/regression-mutation/$mutant_number"

if [ $mutant_compiles -eq 0 ]; then
	# Run the mutation analysis here
	out=$(java -cp "$subject_cp" org.junit.runner.JUnitCore RegressionTest)
    fail=$(echo "$out" | grep "Tests run: \|OK (")
    echo "Result: "$fail
    tmp=$(echo ${fail} | cut -d'(' -f 3)
    echo $tmp
    if [ ! -z "${tmp}" ]; then
    	echo "Mutant $mutant_number killed? : 1" > "output/$subject_name/regression-mutation/$mutant_number/regression-mutant-results.txt"
    else
    	echo "Mutant $mutant_number killed? : 0" > "output/$subject_name/regression-mutation/$mutant_number/regression-mutant-results.txt"
    fi
    
else
    echo "Mutant $mutant_number killed? : 1" > "output/$subject_name/regression-mutation/$mutant_number/regression-mutant-results.txt"
fi

cp $SUBJECTS_DIR/$subject_name/mutants/original-class/$subject_name.java $orig_class_path

cd "$SUBJECTS_DIR/$subject_name/"
./gradlew jar
cd $CURR_DIR
