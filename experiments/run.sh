source experiments/init_env.sh

subject_name=$1
subject_set=$2

subject_cp="$SUBJECTS_DIR/$subject_name/build/libs/*"

input_file="experiments/$subject_set-subjects/$subject_name.properties"
omit_methods_file="$OUTPUTS_DIR/$subject_name/pure-methods.txt"
java -cp "$subject_cp:$RANDOOP_DIR/randoop-all-4.3.1.jar" -Xbootclasspath/a:${RANDOOP_DIR}/replacecall-4.3.1.jar -javaagent:${RANDOOP_DIR}/replacecall-4.3.1.jar randoop.main.Main gentests --classlist=$input_file --omit-methods-file=$omit_methods_file --output-limit=2000 --time-limit=0 --literals-level=ALL --literals-file=literals/lits.txt --deterministic=true --forbid-null=true
