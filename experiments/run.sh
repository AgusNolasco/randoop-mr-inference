source experiments/init_env.sh

subject_name=$1

subject_cp="$SUBJECTS_DIR/$subject_name/build/libs/*"

input_file="experiments/$subject_name.properties"

java -cp "$subject_cp:$JAR" randoop.main.Main gentests --testclass=com.example.myboundedstack.MyBoundedStack --output-limit=500 --literals-level=ALL --literals-file=literals/lits.txt