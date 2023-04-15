import os
import subprocess
import csv
import sys

outputs_dir = 'output'

subject_set   = sys.argv[1]
gen_strategy  = sys.argv[2]
mrs_to_fuzz   = sys.argv[3]
allow_epa_loops = sys.argv[4]

dir_path = f'experiments/{subject_set}-subjects'
i = 0
for filename in os.listdir(dir_path):
    if '.properties' in filename:
        subject_name = filename.split('.properties')[0]
        print('Running: ' + subject_name)
        result = subprocess.run(f'experiments/run.sh {subject_set} {subject_name} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops}', shell=True, stdout=subprocess.PIPE)

subprocess.run(f'python3 gen-summary.py {subject_set} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops}', shell=True, stdout=subprocess.PIPE)
