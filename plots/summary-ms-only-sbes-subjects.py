import sys
import pandas as pd
import os
import statistics
import matplotlib.pyplot as plt
import numpy as np
import plotly.express as px
import plotly.graph_objects as go
import plotly.io as pio 

pio.kaleido.scope.mathjax = None

folders_to_process = 'from-friday/output'

mut_scores_regression = []
mut_scores_epa = []
mut_scores_epa_regression = []
mut_scores_epa_sat = []
mut_scores_epa_sat_regression = []
mut_scores_epa_random = []
mut_scores_sbes = []
mut_scores_sbes_regression = []

simulations = 100
subjs = []

total_inferred_epa = 0
reduced_set = []

only_sbes_subjects = ["Stack","SingleNode","MultiNode","AbstractEdge","Path","Vector2","Vector3"]

for subject_name in only_sbes_subjects:
#for subject_name in os.listdir(folders_to_process):
	#if (subject_name.endswith("csv")):
	#	continue
	print(f'subject: {subject_name}')

	# Load csvs into dataframes
	regression_csv = f'from-friday/evo-epa/output/{subject_name}/regression-mutation/summary.csv'
	epa_only_csv = f'from-friday/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-INFERRED.csv' 
	epa_sat_csv = f'from-friday/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-REDUCED.csv'
	epa_random_csv = f'from-friday/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-RANDOM-SELECTION-{simulations}.csv'

	df_regression = pd.read_csv(regression_csv)
	# Regression mut score
	total_killed = 0
	mutants = len(df_regression.columns)-1
	for column in df_regression:
		if (column=='Unnamed: 0'):
			continue
		if (1 in df_regression[column].unique()):
			total_killed+=1
	ms_regression = round(total_killed/mutants*100,2)
	mut_scores_regression.append(ms_regression)

	if (not os.path.exists(epa_only_csv)):
		mut_scores_epa.append(0)
		mut_scores_epa_regression.append(ms_regression)
		mut_scores_epa_sat.append(0)
		mut_scores_epa_sat_regression.append(ms_regression)
		mut_scores_epa_random.append(0)
		continue
	
	df_epa = pd.read_csv(epa_only_csv)
	df_epa_sat = pd.read_csv(epa_sat_csv)
	df_epa_random = pd.read_csv(epa_random_csv)


	# Regression + EPA mut score
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

	ms_epa = round(killed_epa/mutants*100,2)
	ms_epa_regression = round(total_killed/mutants*100,2)
	inf_epa = len(df_epa.index)
	mut_scores_epa.append(ms_epa)
	mut_scores_epa_regression.append(ms_epa_regression)
	subjs.append(subject_name)

	# Regression + EPA + SAT
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

	ms_epa_sat = round(killed_epa_sat/mutants*100,2)
	ms_epa_sat_regression = round(total_killed/mutants*100,2)
	inf_epa_sat = len(df_epa_sat.index)
	mut_scores_epa_sat.append(ms_epa_sat)
	mut_scores_epa_sat_regression.append(ms_epa_sat_regression)

	# Random selection avg
	avg_mut_score = df_epa_random["avg_mut_score"].unique()[0]
	mut_scores_epa_random.append(avg_mut_score)
	print(f'inferred --> epa: {inf_epa} - epa+sat: {inf_epa_sat}')
	total_inferred_epa += inf_epa
	reduced_set.append(inf_epa_sat)
	print(f'mut score --> epa: {ms_epa} - epa+sat: {ms_epa_sat} - epa+random: {avg_mut_score}')
	print()

total_reduced_sat = sum(reduced_set)
reduced_avg = round(total_reduced_sat/len(reduced_set),2)
print('Summary')
percentage_reduction = round((total_inferred_epa-total_reduced_sat)/total_inferred_epa,2)
print(f'inferred total: {total_inferred_epa} - reduced sat: {total_reduced_sat} - ')
print(f'reduction: {percentage_reduction}%')
print(f'mean reported reduction: {reduced_avg}')

print()
print('SBES')
for subject_name in only_sbes_subjects:
	#if (subject_name.endswith("csv")):
	#	continue

	regression_csv = f'from-friday/evo-epa/output/{subject_name}/regression-mutation/summary.csv'
	df_regression = pd.read_csv(regression_csv)

	# Regression + SBES mut score
	killed_sbes = 0
	total_killed = 0
	killed_by_both = df_regression.copy()
	mutants = len(df_regression.columns)-1
	for column in df_regression:
		if (column=='Unnamed: 0'): 
			continue

		if (os.path.exists(f'output/{subject_name}/sbes-mutation/{column}/SBES-mutant-results.txt')):
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

		if 1 in killed_by_both[column].unique():
			total_killed += 1

	if not np.isnan(killed_sbes):
		ms_sbes = round(killed_sbes/mutants*100,2)
		print(f'subject: {subject_name} - score: {ms_sbes}')
		mut_scores_sbes.append(ms_sbes)
	
	ms_sbes_regression = round(total_killed/mutants*100,2)
	mut_scores_sbes_regression.append(ms_sbes_regression)
	print(f'subject: {subject_name} - score sbes+reg: {ms_sbes_regression}')

#print('subjects: ',subjs)
print()
print(f'mut scores regression: {mut_scores_regression}')
print(f'mut scores epa: {mut_scores_epa}')
print(f'mut scores epa + regression: {mut_scores_epa_regression}')
print(f'mut scores epa + sat: {mut_scores_epa_sat}')
print(f'mut scores epa + sat + regression: {mut_scores_epa_sat_regression}')
print(f'mut scores epa + random: {mut_scores_epa_random}')
mut_scores_sbes = [50.1,0.4,0.4,0.8,2.5,54.4,34.9]
print(f'mut scores sbes: {mut_scores_sbes}')
print(f'mut scores sbes + regression: {mut_scores_sbes_regression}')

print()
reg_avg = round(statistics.mean(mut_scores_regression),2)
epa_avg = round(statistics.mean(mut_scores_epa),2)
epa_reg_avg = round(statistics.mean(mut_scores_epa_regression),2)
epa_sat_avg = round(statistics.mean(mut_scores_epa_sat),2)
epa_sat_reg_avg = round(statistics.mean(mut_scores_epa_sat_regression),2)
epa_random_avg = round(statistics.mean(mut_scores_epa_random),2)
sbes_avg = round(statistics.mean(mut_scores_sbes),2)
sbes_reg_avg = round(statistics.mean(mut_scores_sbes_regression),2)


print("Average Mut Score for the SBES subjects")
print('Regression: ',reg_avg)
print()
print('MemoRIA(all): ',epa_avg)
print('Regression + MemoRIA(all): ',epa_reg_avg)
print()
print('MemoRIA(sat): ',epa_sat_avg)
print('Regression + MemoRIA(sat) avg: ',epa_sat_reg_avg)
#print('epa + random avg: ',epa_random_avg)
print()
print('SBES: ',sbes_avg)
#print(mut_scores_sbes)
print('Regression + SBES: ',sbes_reg_avg)
print()

avgs = [reg_avg, epa_reg_avg, epa_sat_reg_avg]
bars = ('Evo+EPA Suites', 'Suites + Inferred MRs', 'Suites + Reduced MRs')
y_pos = np.arange(len(bars))
data = [mut_scores_regression, mut_scores_epa_regression, mut_scores_epa_sat_regression]


#fig.update_yaxes(showgrid=True, gridwidth=1, gridcolor='lightgrey')
print('Mut score plot regression data:')
print(f'Evo+EPA median: {np.median(mut_scores_regression)}')
print(f'SBES median: {np.median(mut_scores_sbes_regression)}')
print(f'MemoRIA(all) median: {np.median(mut_scores_epa_regression)}')
print(f'MemoRIA(sat) median: {np.median(mut_scores_epa_sat_regression)}')
print()



