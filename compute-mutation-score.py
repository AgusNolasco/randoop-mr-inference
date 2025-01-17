import os
import subprocess
import csv
import sys
import re
import numpy as np
import pandas as pd

# Usage: python3 compute-mutation-score.py {EPA|SBES} {subject_name} {gen_strategy} {mrs_to_fuzz} {allow_epa_loops}

def split_mr(mr):
    precond = mr.split(' -> ')[0]
    precond = precond[1:len(precond)-1]
    preconds = precond.split(', ')
    mrs = []
    for cond in preconds:
        mrs.append(f'[{cond}] -> {mr.split(" -> ")[1]}')
    return mrs

def split_mrs(mrs):
    split_mrs = []
    for mr in mrs:
        split_mrs.extend(split_mr(mr))
    return split_mrs

outputs_dir = 'output'

subject_set   = sys.argv[1]
subject_name  = sys.argv[2]
gen_strategy  = sys.argv[3]
mrs_to_fuzz   = sys.argv[4]
allow_epa_loops = sys.argv[5]
seeds = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

subjects_dir = '../epa-benchmark-subjects/subjects/'

mutant_numbers = []
with open(f'{subjects_dir}/{subject_name}/mutants.log') as f:
    mutants = [line.rstrip() for line in f]
    for mutant in mutants:
        pattern = re.compile(".*is.*Enabled.*|.*toString.*|.*equals.*|.*\$.*")
        if not pattern.match(mutant):
            mutant_numbers.append(int(mutant.split(':')[0]))
    print(f'Number of total mutants: {len(mutants)}')

print(f'Number of interest mutants: {len(mutant_numbers)}')

mutants_dir = f'{subjects_dir}/{subject_name}/mutants/'

for seed in seeds:
    for over in ['INFERRED','REDUCED']:
        path_to_mrs = ''
        if over == 'REDUCED':
            path_to_mrs = f'../alloy-mr-reducer/output/{subject_name}/allow_epa_loops_{allow_epa_loops}/{gen_strategy}/{mrs_to_fuzz}/{seed}/mrs.txt'
        elif over == 'INFERRED':
            path_to_mrs = f'output/{subject_name}/allow_epa_loops_{allow_epa_loops}/{gen_strategy}/{mrs_to_fuzz}/{seed}/log.txt'

        mrs = []
        with open(path_to_mrs) as mrs_file:
            mrs = [line.rstrip() for line in mrs_file]
            if over == 'REDUCED':
                first_mark = mrs.index('')
                mrs = mrs[first_mark+1:]
            elif over == 'INFERRED':
                first_mark = mrs.index('Valid MRs:')
                mrs = mrs[first_mark+2:]
            second_mark = mrs.index('')
            mrs = mrs[:second_mark]


        mrs = split_mrs(mrs)

        mrs_per_mutant = dict()
        mutant_numbers_to_remove = []
        for mutant_number in mutant_numbers:
            path_to_mrs = f'output/{subject_name}/allow_epa_loops_{allow_epa_loops}/{gen_strategy}/{mrs_to_fuzz}/{seed}/mutants/{mutant_number}/log.txt'
            if not os.path.isfile(path_to_mrs):
                print(f'Mutant: {mutant_number} removed because not compile or randoop has a problem with it')
                mutant_numbers_to_remove.append(mutant_number)
                continue
            mutant_mrs = []
            with open(path_to_mrs) as mrs_file:
                try:
                    mutant_mrs = [line.rstrip() for line in mrs_file]
                    first_mark = mutant_mrs.index('Valid MRs:')
                    mutant_mrs = mutant_mrs[first_mark+2:]
                    second_mark = mutant_mrs.index('')
                    mutant_mrs = mutant_mrs[:second_mark]
                except:
                    mutant_mrs = []
            mutant_mrs = split_mrs(mutant_mrs)
            mrs_per_mutant[mutant_number] = mutant_mrs

        mutant_numbers = set(mutant_numbers) - set(mutant_numbers_to_remove)

        data = pd.DataFrame(data=np.zeros((len(mrs), len(mutant_numbers)), dtype=int), index=mrs, columns=list(mutant_numbers))

        for mutant_number in mutant_numbers:
            mrs_killing_mutant = list(set(mrs) - set(mrs_per_mutant[mutant_number]))
            data.loc[mrs_killing_mutant, mutant_number] = 1

        data.to_csv(f'output/{subject_name}/allow_epa_loops_{allow_epa_loops}/{gen_strategy}/{mrs_to_fuzz}/{seed}/mutants/mutation-results-{over}.csv')
