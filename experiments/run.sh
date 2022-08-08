source experiments/init_env.sh

subject_name=$1

subject_cp="$SUBJECTS_DIR/$subject_name/build/libs/*"

input_file="experiments/$subject_name.properties"
omit_methods_file="$OUTPUTS_DIR/$subject_name/pure-methods.txt"
java -cp "$subject_cp:$JAR" randoop.main.Main gentests --classlist=$input_file --omit-methods-file=$omit_methods_file --output-limit=500 --literals-level=ALL --literals-file=literals/lits.txt