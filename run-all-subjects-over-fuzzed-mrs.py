import os
import subprocess
import csv
import sys

outputs_dir = 'output'

subject_set   = sys.argv[1]
mrs_to_fuzz   = sys.argv[2]
allow_epa_loops = 'true'
seeds = [0, 1, 2, 3, 4, 5, 6, 7, 8 , 9]

dir_path = f'experiments/{subject_set}-subjects'
i = 0
for filename in os.listdir(dir_path):
    if '.properties' in filename:
        subject_name = filename.split('.properties')[0]
        for seed in seeds:
            result = subprocess.run(f'experiments/run-over-fuzzed-mrs.sh {subject_set} {subject_name} {mrs_to_fuzz} {allow_epa_loops} {seed}', shell=True, stdout=subprocess.PIPE)
