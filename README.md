A parser for wiktionary spanish definition dumps.  Parses definitions
and the meta-data about the words such as tense, mood, and lemma.

Example:

It parses lines like:
Spanish	comerán	Verb	# {{es-verb form of|person=third-person|number=plural|tense=future|mood=indicative|ending=er|comer}}


into structures like:

{:word comerán
 :tense "future"
 :lemma "comer"
 :ending :er
 :mood :indicative
 ....}
