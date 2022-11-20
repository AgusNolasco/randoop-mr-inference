import os
import subprocess
import csv
import sys

outputs_dir = 'output'

subject_set = sys.argv[1]

dir_path = f'experiments/{subject_set}-subjects'
i = 0
for filename in os.listdir(dir_path):
    if '.properties' in filename:
        subject_name = filename.split('.properties')[0]
        print('Running: ' + subject_name)
        result = subprocess.run(f'experiments/run.sh {subject_name} {subject_set}', shell=True, stdout=subprocess.PIPE)
        output = result.stdout.decode('utf-8')
        print(output)
        f = open(f'{outputs_dir}/{subject_name}.txt', 'w')
        f.write(output)
        f.close()
