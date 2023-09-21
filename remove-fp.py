import sys
import pandas as pd
import re
import datetime
import shutil
import os
import subprocess

subject_set = sys.argv[1]
subject_name = sys.argv[2]
selected_seed = sys.argv[3]

path_to_dir = f'output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000'

filename = f'{path_to_dir}/{selected_seed}/mutants/mutation-results-REDUCED.csv'

df = pd.read_csv(filename, header=[0], index_col=[0])
print(f'shape: {df.shape}')

properties = {}
i = 1
for mr in df.index:
    properties[i] = (df.loc[mr].sum(), mr)
    i += 1

for num in properties.keys():
    print(f'{num}. {properties[num][0]} - {properties[num][1]}')

print("\nSelect the properties to be removed (0 to finish):")

props_to_remove = []
while True:
    prop_num = int(input())
    if prop_num == 0:
        break
    props_to_remove.append(properties[prop_num][1])

if not props_to_remove:
    exit()

time = datetime.datetime.now()

seeds = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

count_rm_log = {}
count_rm_mrs_alloy = {}
for seed in seeds:
    count_rm_log[seed] = 0
    count_rm_mrs_alloy[seed] = 0

for seed in seeds:
    # list to store file lines
    logfile = f'{path_to_dir}/{seed}/log.txt'
    lines = []
    with open(logfile, 'r') as fp:
        lines = fp.readlines()

    shutil.copy(logfile, f'{logfile}_{time.day}-{time.month}-{time.year}_{time.hour}:{time.minute}')

    # Write file
    with open(logfile, 'w') as fp:
        for line in lines:
            remove = False
            for prop in props_to_remove:
                if re.match(f'^{re.escape(prop)}$', line):
                    remove = True
            if not remove:
                fp.write(line)
            else:
                count_rm_log[seed] = count_rm_log[seed] + 1

    mrtoalloyfile = f'{path_to_dir}/{seed}/mrs-alloy-predicates.als'
    lines = []
    with open(mrtoalloyfile, 'r') as fp:
        lines = fp.readlines()

    shutil.copy(mrtoalloyfile, f'{mrtoalloyfile}_{time.day}-{time.month}-{time.year}_{time.hour}:{time.minute}')

    # Write file
    with open(mrtoalloyfile, 'w') as fp:
        for line in lines:
            remove = False
            for prop in props_to_remove:
                if re.match(f'^{re.escape(prop)}\ #', line):
                    remove = True
            if not remove:
                fp.write(line)
            else:
                count_rm_mrs_alloy[seed] = count_rm_mrs_alloy[seed] + 1

for seed in seeds:
    if count_rm_mrs_alloy[seed] != count_rm_log[seed]:
        print(f'For seed {seed} in mrs_alloy file were removed {count_rm_mrs_alloy[seed]} properties but in log only {count_rm_log[seed]}')
    

cwd = os.getcwd()

os.chdir('../alloy-mr-reducer')

for seed in seeds:
    reduc_mrs = f'{path_to_dir}/{seed}/mrs.txt'
    formatted_mrs = f'{path_to_dir}/{seed}/formatted-mrs.csv'
    reduc_log = f'{path_to_dir}/{seed}/log.txt'

    shutil.copy(reduc_mrs, f'{reduc_mrs}_{time.day}-{time.month}-{time.year}_{time.hour}:{time.minute}')
    shutil.copy(formatted_mrs, f'{formatted_mrs}_{time.day}-{time.month}-{time.year}_{time.hour}:{time.minute}')
    shutil.copy(reduc_log, f'{reduc_log}_{time.day}-{time.month}-{time.year}_{time.hour}:{time.minute}')

subprocess.run(f'experiments/run.sh {subject_name} EPA_AWARE 1000 true {selected_seed}', shell=True)
os.chdir(cwd)

subprocess.run(f'python3 compute-mutation-score.py {subject_set} {subject_name} EPA_AWARE 1000 true', shell=True, stdout=subprocess.PIPE)
subprocess.run(f'python3 mrs_plus_regression_assertions_mut_score.py {subject_name} {selected_seed}', shell=True)
