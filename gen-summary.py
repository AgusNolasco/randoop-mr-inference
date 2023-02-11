import os
import subprocess
import csv
import sys

header = ['Class', 'Input', 'Output', 'Valid %', 'Invalid %']
rows = []

gen_strategy = sys.argv[1]
mrs_to_fuzz = sys.argv[2]
allow_epa_loops = sys.argv[3]
random = False
if len(sys.argv) == 5 and sys.argv[4] == 'random':
    random = True

dir_path = 'output'
for subject_dir in os.listdir(dir_path):
    if os.path.isdir(f'{dir_path}/{subject_dir}'):
        path_to_output_dir = f'{dir_path}/{subject_dir}/allow_epa_loops_{allow_epa_loops}/{gen_strategy}/{mrs_to_fuzz}/'
        if random:
            path_to_output_dir += 'random/'
        with open(path_to_output_dir + 'log.txt') as f:
            lines = f.readlines()
            clazz = [line for line in lines if 'Class: ' in line][0].split(': ')[1].strip()
            accept_percentage = float([line for line in lines if '% of valid MRs: ' in line][0].split(': ')[1])
            reject_percentage = float([line for line in lines if '% of invalid MRs: ' in line][0].split(': ')[1])
            input1 = [line.strip() for line in lines if 'Input: ' in line][0].split(': ')[1].split(' ')[1]
            input1 = int(input1) if input1 else 0
            output = [line.strip() for line in lines if 'Output: ' in line][0].split(': ')[1].split(' ')[0]
            if not output:
                output = 0
            else:
                output = int(output)

            rows.append([clazz, input1, output, accept_percentage, reject_percentage])

if random:
    filename = f"output/{gen_strategy}-{mrs_to_fuzz}-{allow_epa_loops}-random-resume.csv"
else:
    filename = f"output/{gen_strategy}-{mrs_to_fuzz}-{allow_epa_loops}-resume.csv"
    
with open(filename, 'w') as csvfile: 
    csvwriter = csv.writer(csvfile) 
    csvwriter.writerow(header) 
    csvwriter.writerows(rows)
