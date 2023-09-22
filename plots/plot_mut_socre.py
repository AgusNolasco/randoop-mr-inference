import sys
import pandas as pd
import os
import statistics
import matplotlib.pyplot as plt
import numpy as np

folders_to_process = 'from-friday/memoria/output'

mut_scores_regression = []
mut_scores_epa = []
mut_scores_epa_regression = []
mut_scores_epa_sat = []
mut_scores_epa_sat_regression = []

for subject_name in os.listdir(folders_to_process):
	
	print(f'subject: {subject_name}')

	# Load csvs into dataframes
	regression_csv = f'from-friday/evo-epa/output/{subject_name}/regression-mutation/summary.csv'
	epa_only_csv = f'from-friday/memoria/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-INFERRED.csv' 
	epa_sat_csv = f'from-friday/memoria/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-REDUCED.csv'
	df_regression = pd.read_csv(regression_csv)
	df_epa = pd.read_csv(epa_only_csv)
	df_epa_sat = pd.read_csv(epa_sat_csv)

	# Regression mut score
	total_killed = 0
	mutants = len(df_epa_sat.columns)-1
	for column in df_epa_sat:
		if (column=='Unnamed: 0'):
			continue
		if (1 in df_regression[column].unique()):
			total_killed+=1
	ms_regression = round(total_killed/mutants*100,2)
	mut_scores_regression.append(ms_regression)

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
	mut_scores_epa.append(ms_epa)
	mut_scores_epa_regression.append(ms_epa_regression)

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
	mut_scores_epa_sat.append(ms_epa_sat)
	mut_scores_epa_sat_regression.append(ms_epa_sat_regression)


print(f'mut scores regression: {mut_scores_regression}')
print(f'mut scores epa: {mut_scores_epa}')
print(f'mut scores epa + regression: {mut_scores_epa_regression}')
print(f'mut scores epa + sat: {mut_scores_epa_sat}')
print(f'mut scores epa + sat + regression: {mut_scores_epa_sat_regression}')

print()
reg_avg = round(statistics.mean(mut_scores_regression),2)
epa_avg = round(statistics.mean(mut_scores_epa),2)
epa_reg_avg = round(statistics.mean(mut_scores_epa_regression),2)
epa_sat_avg = round(statistics.mean(mut_scores_epa_sat),2)
epa_sat_reg_avg = round(statistics.mean(mut_scores_epa_sat_regression),2)
print('regression avg: ',reg_avg)
print('epa avg: ',epa_avg)
print('regression + epa avg: ',epa_reg_avg)
print('regression + epa + sat avg: ',epa_sat_reg_avg)

# Make a random dataset:
avgs = [reg_avg, epa_reg_avg, epa_sat_reg_avg]
bars = ('Evo+EPA Suites', 'Suites + Inferred MRs', 'Suites + Reduced MRs')
y_pos = np.arange(len(bars))

# Create bars

fig, ax = plt.subplots()

rect1 = ax.bar(y_pos, avgs, color=['grey', 'royalblue', 'orange'])

# Create names on the x-axis
plt.xticks(y_pos, bars)

def autolabel(rects):
    """
    Attach a text label above each bar displaying its height
    """
    for rect in rects:
        height = rect.get_height()
        ax.text(rect.get_x() + rect.get_width()/2., 1.05*height,
                '%d' % int(height),
                ha='center', va='bottom')

autolabel(rect1)
ax.set_ylabel('Average Mutation Score')
plt.ylim(0, 100)

# Show graphic
plt.show()

