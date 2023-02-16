import sys
import pandas as pd
import os

outputs_dir = 'output'

subject_name  = sys.argv[1]

# Csv files
regression_csv = f'{outputs_dir}/{subject_name}/regression-mutation/summary.csv'
random_csv = f'{outputs_dir}/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-RANDOM.csv'
epa_only_csv = f'{outputs_dir}/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-INFERRED.csv' 
epa_sat_csv = f'{outputs_dir}/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-REDUCED.csv'
sbes_csv = ''

df_regression = pd.read_csv(regression_csv)
df_random = pd.read_csv(random_csv)
df_epa = pd.read_csv(epa_only_csv)
df_epa_sat = pd.read_csv(epa_sat_csv)

# Mut score for regression
total_killed = 0
print(df_epa_sat.shape)
mutants = len(df_epa_sat.columns)-1
for column in df_epa_sat:
	if (column=='Unnamed: 0'):
		continue
	if (1 in df_regression[column].unique()):
		total_killed+=1
mut_score_regression = total_killed/mutants*100

# Mut score for regression + Random
killed_by_both = df_regression.copy()
total_killed = 0
killed_random = 0
for column in df_random:
	if (column=='Unnamed: 0'): 
		continue
	if (1 in df_random[column].unique()):
		killed_by_both[column] = 1
		killed_random += 1
	if (1 in killed_by_both[column].unique()):
		total_killed += 1

mut_score_random = killed_random/mutants*100

print()
print('---- Regression + Random ----')
print(f'mut score regression: {mut_score_regression}')
print(f'mut score random: {mut_score_random}')
print(f'killed both: {total_killed} - mutants {mutants}')
print(f'mut score both: {total_killed/mutants*100}')
print()

# Mut score for regression + EPA
killed_by_both = df_regression.copy()
total_killed = 0
killed_epa = 0
for column in df_epa:
	if (column=='Unnamed: 0'): 
		continue
	if (1 in df_epa[column].unique()):
		killed_by_both[column] = 1
		killed_epa += 1
	if (1 in killed_by_both[column].unique()):
		total_killed += 1

mut_score_epa = killed_epa/mutants*100

print()
print('---- Regression + EPA ----')
print(f'mut score regression: {mut_score_regression}')
print(f'mut score epa: {mut_score_epa}')
print(f'killed both: {total_killed} - mutants {mutants}')
print(f'mut score both: {total_killed/mutants*100}')
print()

# Mut score for regression + EPA+SAT

killed_by_both = df_regression.copy()
total_killed = 0
killed_epa_sat = 0
for column in df_epa_sat:
	if (column=='Unnamed: 0'): 
		continue
	if (1 in df_epa_sat[column].unique()):
		killed_by_both[column] = 1
		killed_epa_sat += 1
	if (1 in killed_by_both[column].unique()):
		total_killed += 1

mut_score_epa_sat = killed_epa_sat/mutants*100

print()
print('---- Regression + EPA+SAT ----')
print(f'mut score regression: {mut_score_regression}')
print(f'mut score epa_sat: {mut_score_epa_sat}')
print(f'killed both: {total_killed} - mutants {mutants}')
print(f'mut score both: {total_killed/mutants*100}')
print()

# Mut score for regression + SBES
killed_by_both = df_regression.copy()
total_killed = 0
killed_sbes = 0
sbes_folder = f'{outputs_dir}/{subject_name}/sbes-mutation'
for column in df_epa_sat:
	if (column=='Unnamed: 0'): 
		continue
	# Check if sbes kills it
	killed = 0
	with open(f'output/{subject_name}/sbes-mutation/{column}/SBES-mutant-results.txt') as f:
		lines = [line.rstrip() for line in f]
		result = lines[0].split(' : ')[1]
		if result == '1':
			killed = 1
	if (killed == 1):
		killed_by_both[column] = 1
		killed_sbes += 1
	if (1 in killed_by_both[column].unique()):
		total_killed += 1

mut_score_sbes = killed_sbes/mutants*100

print()
print('---- Regression + SBES ----')
print(f'mut score regression: {mut_score_regression}')
print(f'killed sbes: {killed_sbes}')
print(f'mut score sbes: {mut_score_sbes}')
print(f'killed both: {total_killed} - mutants {mutants}')
print(f'mut score both: {total_killed/mutants*100}')
print()
