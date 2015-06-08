# Nieuwsbrief generator
## Introductie
De nieuwsbrief generator is geschreven als tussenoplossing voor de nieuwsbrief van Studievereniging Thalia.	Het is geschreven door Maurice Knoop.		
			
## Werking
Stap 1: Zorg dat je in een .xml de nieuwsbrief netjes en foutloos opstelt.
Stap 2: start nieuwsbrief-generator.jar of gebruik generator.bat 			
Stap 3: Selecteer het document waar de nieuwsbrief in is gedefinieerd. Dit moet een .xml bestand zijn.
Stap 4: Selecteer de output locatie. 

Volg hierna de stappen van het programma. Controleer bij het overzicht van wat er in de nieuwsbrief komt of alles klopt.

De generator zal dan het bestand inlezen en een nieuwsbrief genereren. De URL voor het bestand zal zijn:
*thalia.nu/nieuwsbrief/YYYY/WW/nieuwsbrief.html*, waar *YYYY* voor het jaar en *WW* voor het weeknummer staat. Voorbeeld: http://www.thalia.nu/nieuwsbrief/2015/01/nieuwsbrief.html.

In het geval van een fout kun je het bron bestand bijwerken en de generator het bestand opnieuw laten lezen. De generator zal aangeven hoe je het moet herstarten
indien het een fout tegenkomt en zodra het overzicht van de gegevens wordt getoond ter controle. Je hoeft dan niet opnieuw de bron of output aan te geven.

Mocht je niet wijs worden uit de foutmelding, start dan generator.bat. Deze toont meer informatie, zoals op welk moment het mis gaat. 


Uiteraard hoeft niet alles perfect te worden gegenereerd: kleine aanpassingen kunnen ook in de HTML worden gedaan.
Let hierbij wel op dat je geen data verliest als je opnieuw genereerd. Sinds versie 1.4 wordt wel gekeken of je output al bestond zodat je niet per ongeluk overschrijft.

## XML/NB invoer

Een nieuwsbrief ziet er uit zoals hieronder gedefinieerd. Een volledig ingevuld voorbeeld, 
in de vorm van de nieuwsbrief van week 01 van 2015, kan worden gevonden in de map "voorbeelden".
```xml			
<nieuwsbrief jaar="..." week-nummer="...">
	<item type="mededeling" titel="Beste Thalianen" auteur="Maurice Knoop">
	Een inleiding
	</item>
	<item type="agenda"/>
	<item type="evenement"
			titel="..."
			wat="..."
			waar="..."
			wanneer="..."
			prijs="..."
			kosten="..."
			inagenda="true|false"
			datumagenda="...">
			<p>...</p>
	</item>
</nieuwsbrief>
```
De elementen uit bovenstaand XML dienen als volgt te worden gebruikt:

element						| Extra	|	Default		|	Beschrijving																						|
-----------|-----------|-----------|-----------
[nieuwsbrief:jaar] 			| V		| not allowed	|	Dit is het jaar waarin de nieuwsbrief is uitgegeven. Enkel voor administratie
[nieuwsbrief:week-nummer]	| V		| not allowed	|	Dit is het weeknummer waarvoor de nieuwsbrief is uitgegeven.				
[item]						|		|				|	Dit is de algemene benaming voor iets dat in de nieuwsbrief staat.
[item:type]					| V		| not allowed	|	Dit is het type voor het item. Altijd "mededeling", "agenda" of "evenement". 
[item:titel]				| V		| not allowed	|	De titel van het stuk. Hier worden automatisch hoofdletters van gemaakt. Dat kan niet anders.	
[type:agenda]				|		|				|	Agenda wordt automatisch aangevuld. Hiervoor hoef je zelfs geen titel op te geven. Als de nieuwsbrief geen agenda nodig heeft, dan laat je dit weg. Meestal plaats je het als tweede									
[type:mededeling]			|		|				|	Mededeling is een type van item. Het is een tekst die door iemand is geschreven en geen evenement is
[mededeling:auteur]			| V		| not allowed	|	De auteur van het stuk. Dit komt onderaan het bericht te staan.	
[mededeling:namens]			|!V		| 				|	Namens wie het stuk is geschreven. Dit staat boven de auteur. Bijvoorbeeld "jullie bestuur"	
[mededeling:auteur_titel]	|!V		| not allowed	|	De titel van de auteur van het stuk. Dit komt onder de auteur te staan. Bijvoorbeeld "Secretaris, 24e bestuur der Studievereniging Thalia.					
[type:evenement]			| !V H	|				|	Algemene definitie voor een evenement. De volgorde waarin evenementen zijn gedefinieerd	bepaalt de volgorde binnen de nieuwsbrief.			
[evenement:titel]			| !V	| [:wat]		|	Een alternative titel voor het evenement. Dit komt groot in de mail te staan. 						
[evenement:wat]	*			| !V	| [:titel]**	|	Korte titel voor het evenement voor in de wat-waar-wanneer.
[evenement:waar] *			| !V	| 				|	Aanduiding van locatie. 
[evenement:wanneer] *		| !V	| 				|	Aanduiding van datum en tijdstip. 
[evenement:prijs] *			| !V g	|				|	Aanduiding van prijs.
[evenement:kosten] 			| !V G	|				|	Aanduiding van de kosten per persoon. Dit werkt in combinatie met [evenement:heeftkorting]. Indien de korting op true staat, en de kosten zijn gedefinieerd, dan zullen de kosten opgenomen worden in de standaard waarschuwing die door [evenement:heeftkorting] wordt geplaatst.	
[evenement:inagenda]		| !V B	| true			|	Aanduiding van of het in de agenda moet worden opgenomen. Als de waarde true is, dan moet [evenement:wanneer] of [evenement:datumagenda] zijn gedefinieerd. Dit levert echter geen fouten op
[evenement:datumagenda]		| !V 	| [:wanneer]	|	Aanduiding van de datum voor in de agenda.


Teken	|	Betekenis
------|------
 V	|	Verplicht. Weglaten lijdt tot crashes of rare output.
!V	|	Niet verplicht. Kan probleemloos worden weggelaten.	
 H	|	Herhaling. Mag worden herhaald.
 B	|	Boolean. Waarde moet Boolean zijn.
 G	|	Geld. Matched op `"\d(,(\d\d|-))?"`. Voorbeelden: 	`5,- 	10,50		1,00`		1	Niet toegestaan:	`1,5		1,		1, 0		1, -`	
 g	| 	Geadviseerd als Geld. Het wordt geadviseerd het te gebruiken als G, maar dit wordt niet afgedwongen.



\* :	Als een van deze elementen is geplaatst, dan wordt een "wat-waar-wanneer" van het evenement geplaatst. Alles wat ontbreekt in de definitie			
	zal ook niet worden opgenomen in de "wat-waar-wanneer". Indien al deze elementen niet zijn gedefinieerd zal geen "wat-waar-wanneer" worden geplaatst

**:	[evenement:titel] wordt alleen gebruikt indien gedefinieerd. Het wordt geen recursieve verwijzing naar elkaar.										



