import sys
import pandas as pd
import os
import statistics
import matplotlib.pyplot as plt
import numpy as np
import plotly.express as px
import plotly.graph_objects as go
import plotly.io as pio

#pio.kaleido.scope.mathjax = None

folders_to_process = 'multiple-runs/output'

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

for subject_name in os.listdir(folders_to_process):
	if (subject_name.endswith("csv")):
		continue
	print(f'subject: {subject_name}')

	# Now we have 10 executions of the same subject, so we need to iterate over them
	all_execs_mut_scores_regression = []
	all_execs_mut_scores_epa = []
	all_execs_mut_scores_epa_regression = []
	all_execs_mut_scores_epa_sat = []
	all_execs_mut_scores_epa_sat_regression = []
	all_execs_mut_scores_epa_random = []
	for execution in os.listdir(f'{folders_to_process}/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/'):
	    if (execution.endswith("als") or execution.endswith("csv") or execution.endswith("txt") or execution == "mutants" or execution == "random"):
	        continue
	    print(f'execution: {execution}')

	    # Load csvs into dataframes
	    regression_csv = f'from-friday/evo-epa/output/{subject_name}/regression-mutation/summary.csv'
	    epa_only_csv = f'multiple-runs/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/{execution}/mutants/mutation-results-INFERRED.csv'
	    epa_sat_csv = f'multiple-runs/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/{execution}/mutants/mutation-results-REDUCED.csv'
	    epa_random_csv = f'from-friday/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-RANDOM-SELECTION-{simulations}.csv'

	    if (not os.path.exists(epa_only_csv) or not os.path.exists(epa_sat_csv) or not os.path.exists(epa_random_csv)):
	        continue

	    df_regression = pd.read_csv(regression_csv)
	    df_epa = pd.read_csv(epa_only_csv)
	    df_epa_sat = pd.read_csv(epa_sat_csv)
	    df_epa_random = pd.read_csv(epa_random_csv)

	    # Regression mut score
	    total_killed = 0
	    mutants = len(df_epa_sat.columns)-1
	    for column in df_epa_sat:
		    if (column=='Unnamed: 0'):
			    continue
		    if (1 in df_regression[column].unique()):
			    total_killed+=1
	    ms_regression = round(total_killed/mutants*100,2)
	    all_execs_mut_scores_regression.append(ms_regression)

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
	    all_execs_mut_scores_epa.append(ms_epa)
	    all_execs_mut_scores_epa_regression.append(ms_epa_regression)

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
	    all_execs_mut_scores_epa_sat.append(ms_epa_sat)
	    all_execs_mut_scores_epa_sat_regression.append(ms_epa_sat_regression)

	    # Random selection avg
	    avg_mut_score = df_epa_random["avg_mut_score"].unique()[0]
	    all_execs_mut_scores_epa_random.append(avg_mut_score)

	    print(f'inferred --> epa: {inf_epa} - epa+sat: {inf_epa_sat}')
	    print(f'mut score --> epa: {ms_epa} - epa+sat: {ms_epa_sat} - epa+random: {avg_mut_score}')
	    print()

	# The for loop is over, so we need to compute the averages
	if (len(all_execs_mut_scores_regression)==0):
	    print(f'no executions for {subject_name}')
	    continue

	avg_regression = round(statistics.mean(all_execs_mut_scores_regression),2)
	avg_epa = round(statistics.mean(all_execs_mut_scores_epa),2)
	avg_epa_regression = round(statistics.mean(all_execs_mut_scores_epa_regression),2)
	avg_epa_sat = round(statistics.mean(all_execs_mut_scores_epa_sat),2)
	avg_epa_sat_regression = round(statistics.mean(all_execs_mut_scores_epa_sat_regression),2)
	avg_epa_random = round(statistics.mean(all_execs_mut_scores_epa_random),2)
	print(f'avg mut score --> epa: {avg_epa} - epa+sat: {avg_epa_sat} - epa+random: {avg_epa_random}')
	print(f'avg mut score with regression --> regression: {avg_regression} - epa: {avg_epa_regression} - epa+sat: {avg_epa_sat_regression}')
	mut_scores_regression.append(avg_regression)
	mut_scores_epa.append(avg_epa)
	mut_scores_epa_regression.append(avg_epa_regression)
	mut_scores_epa_sat.append(avg_epa_sat)
	mut_scores_epa_sat_regression.append(avg_epa_sat_regression)
	mut_scores_epa_random.append(avg_epa_random)
	subjs.append(subject_name)

print()
print('SBES')
for subject_name in os.listdir('output'):
	if (subject_name.endswith("csv")):
		continue

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


print('regression avg: ',reg_avg)
print('epa avg: ',epa_avg)
print('regression + epa avg: ',epa_reg_avg)
print('epa sat avg: ',epa_sat_avg)
print('regression + epa + sat avg: ',epa_sat_reg_avg)
print('epa + random avg: ',epa_random_avg)
print('sbes avg: ',sbes_avg)
#print(mut_scores_sbes)
print('regression + sbes avg: ',sbes_reg_avg)

avgs = [reg_avg, epa_reg_avg, epa_sat_reg_avg]
bars = ('Evo+EPA Suites', 'Suites + Inferred MRs', 'Suites + Reduced MRs')
y_pos = np.arange(len(bars))
data = [mut_scores_regression, mut_scores_epa_regression, mut_scores_epa_sat_regression]


fig = go.Figure()
fig.add_trace(go.Box(y=mut_scores_regression, name="Evo+EPA"))
fig.add_trace(go.Box(y=mut_scores_sbes_regression, name="SBES"))
fig.add_trace(go.Box(y=mut_scores_epa_regression, name="MemoRIA(all)"))
fig.add_trace(go.Box(y=mut_scores_epa_sat_regression, name="MemoRIA(sat)"))
fig.update_layout(
	#xaxis_title="Technique",
	yaxis_title="Mutation Score (%)",
	#paper_bgcolor='rgba(0,0,0,0)'
	#plot_bgcolor='rgba(0,0,0,0)',
	template='plotly_white'
	#margin_l=0,
	#margin_r=0,
	#margin_b=0,
	#margin_t=0
)
#fig.update_yaxes(showgrid=True, gridwidth=1, gridcolor='lightgrey')
print('Mut score plot regression data:')
print(f'Evo+EPA median: {np.median(mut_scores_regression)}')
print(f'SBES median: {np.median(mut_scores_sbes_regression)}')
print(f'MemoRIA(all) median: {np.median(mut_scores_epa_regression)}')
print(f'MemoRIA(sat) median: {np.median(mut_scores_epa_sat_regression)}')
print()
#fig.show()
if not os.path.exists("images"):
	os.mkdir("images")
fig.write_image("images-new/mut-score-plus-regression.pdf")

fig1 = go.Figure()
fig1.add_trace(go.Box(y=mut_scores_epa, name="MemoRIA(all)"))
fig1.add_trace(go.Box(y=mut_scores_epa_sat, name="MemoRIA(SAT)"))
fig1.add_trace(go.Box(y=mut_scores_epa_random, name="MemoRIA(RANDOM)"))
fig1.add_trace(go.Box(y=mut_scores_sbes, name="SBES"))



fig1.write_image("images-new/mut-score-comparison.pdf")



loc = 0
sat_best = []
random_best = []
equals = []
for random_score in mut_scores_epa_random:
	sat_score = mut_scores_epa_sat[loc]
	if (sat_score > random_score):
		sat_best.append(sat_score)
		random_best.append(np.nan)
		equals.append(np.nan)
	if (random_score > sat_score):
		random_best.append(sat_score)
		sat_best.append(np.nan)
		equals.append(np.nan)
	if (sat_score == random_score):
		equals.append(sat_score)
		sat_best.append(np.nan)
		random_best.append(np.nan)
	loc += 1

fig2 = go.Figure()
print(mut_scores_epa_random)
print(sat_best)
print(random_best)
fig2.add_trace(go.Scatter(mode='markers',x=mut_scores_epa_random,y=sat_best, name="SAT > Random"))
fig2.add_trace(go.Scatter(mode='markers',x=mut_scores_epa_random,y=random_best, name="Random > SAT"))
fig2.add_trace(go.Scatter(mode='markers',x=mut_scores_epa_random,y=equals, name="Equals"))

xs = [i for i in range(1,66)]
fig2.add_trace(go.Line(x=xs, y=xs,
					mode='lines',
					line = dict(dash='dot',color='black', width=0.5),
					showlegend=False))

fig2.update_layout(
	xaxis_title="Mut. score with Random selection (%)",
	yaxis_title="Mut. score with SAT-based selection (%)",
	#paper_bgcolor='rgba(0,0,0,0)'
	#plot_bgcolor='rgba(0,0,0,0)',
	template='plotly_white'
)

fig2.write_image("images-new/sat-vs-random.pdf")

fig3 = go.Figure()

ms_epa_sorted = sorted(mut_scores_epa_sat)
ms_all_sorted = [x for _,x in sorted(zip(mut_scores_epa_sat, mut_scores_epa))]
ms_random_sorted = [x for _,x in sorted(zip(mut_scores_epa_sat, mut_scores_epa_random))]
subjects = [x for _,x in sorted(zip(mut_scores_epa_sat, subjs))]

fig3.add_trace(go.Bar(name='ALL',x=subjects,y=ms_all_sorted))
fig3.add_trace(go.Bar(name='SAT',x=subjects,y=ms_epa_sorted))
fig3.add_trace(go.Bar(name='Random',x=subjects,y=ms_random_sorted))
fig3.update_layout(barmode='group')
fig3.write_image("images-new/sat-vs-random-bar.pdf")
