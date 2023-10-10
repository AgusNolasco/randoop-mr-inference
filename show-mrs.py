import sys
import pandas as pd
import re
import datetime
import shutil
import os
import subprocess

subject_set = sys.argv[1]
subject_name = sys.argv[2]

path_to_dir = f'output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000'

seeds = [0,1,2,3,4,5,6,7,8,9]

properties = set()

for seed in seeds:
    filename = f'{path_to_dir}/{seed}/mutants/mutation-results-REDUCED.csv'
    df = pd.read_csv(filename, header=[0], index_col=[0])
    for mr in df.index:
        properties.add(mr)

print(*properties, sep="\n")
