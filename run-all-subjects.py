import os
import subprocess
import csv

outputs_dir = 'output'

dir_path = 'experiments/'
i = 0
for filename in os.listdir(dir_path):
    if '.properties' in filename:
        subject_name = filename.split('.properties')[0]
        result = subprocess.run(f'experiments/run.sh {subject_name}', shell=True, stdout=subprocess.PIPE)
        output = result.stdout.decode('utf-8')
        print(output)
        f = open(f'{outputs_dir}/{subject_name}.txt', 'w')
        f.write(output)
        f.close()
