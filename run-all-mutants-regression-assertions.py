import os
import subprocess
import csv
import sys
import re
import numpy as np
import pandas as pd

# Usage: python3 run-all-mutants-regression-assertions.py {EPA|SBES} {subject_name}

outputs_dir = 'output'

subject_set   = sys.argv[1]
subject_name  = sys.argv[2]

subjects_dir = '../epa-benchmark-subjects/subjects'

mutant_numbers = []
with open(f'{subjects_dir}/{subject_name}/mutants.log') as f:
    mutants = [line.rstrip() for line in f]
    for mutant in mutants:
        pattern = re.compile(".*is.*Enabled.*|.*toString.*|.*equals.*|.*\$.*")
        if not pattern.match(mutant):
            mutant_numbers.append(int(mutant.split(':')[0]))
            print(mutant)
    print(f'Number of total mutants: {len(mutants)}')

print(f'Number of interest mutants: {len(mutant_numbers)}')

mutants_dir = f'{subjects_dir}/{subject_name}/mutants/'

for mutant_number in mutant_numbers:
    print(f'Running over mutant {mutant_number} for: {subject_name} on regression assertions')
    result = subprocess.run(f'experiments/run-regression-assertions-on-mutant.sh {subject_set} {subject_name} {mutant_number}', shell=True, stdout=subprocess.PIPE)

mutants_killed = 0
for mutant_number in mutant_numbers:
    with open(f'output/{subject_name}/regression-mutation/{mutant_number}/regression-mutant-results.txt') as f:
        lines = [line.rstrip() for line in f]
        result = lines[0].split(' : ')[1]
        if result == '1':
            mutants_killed += 1

print(f'Mutation score for mutant {mutant_number}: {mutants_killed/len(mutant_numbers)}')
