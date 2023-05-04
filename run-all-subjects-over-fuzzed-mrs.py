import os
import subprocess
import csv
import sys

outputs_dir = 'output'

subject_set   = sys.argv[1]
mrs_to_fuzz   = sys.argv[2]
allow_epa_loops = 'true'

dir_path = f'experiments/{subject_set}-subjects'
i = 0
for filename in os.listdir(dir_path):
    if '.properties' in filename:
        subject_name = filename.split('.properties')[0]
        print('Running: ' + subject_name)
        result = subprocess.run(f'experiments/run-over-fuzzed-mrs.sh {subject_set} {subject_name} {mrs_to_fuzz} {allow_epa_loops}', shell=True, stdout=subprocess.PIPE)
