# ttv_chord_schiffeversenken

## working Branch benutzen!
master branch nicht ausführbar
### TODO
- ~~nicht eigene IDs beschießen~~
- ~~reales Game starten~~
- ~~evlt Taktik ausbauen (nicht zufällig)~~

## Taktik
nächste Zielperson:
- ermittle Gegner mit den meisten zerstörten Schiffen
- falls mehrere Gegner gleich viele zerstörte Schiffe haben, wird der bisher angegriffene Gegner ausgewählt
- (alternativ greife Gegner mit den am meisten bekannten Feldern an)

nächstes Ziel:
- Nehme die ID des Gegners und die des bekannten Spielers vor ihm in dem Ring
- Bilde ein Intervall aus beiden IDs und spanne über dieses 100 Felder
- Trage bekannte Fehlschüsse und Treffer von uns und anderen Spielern in die Felder ein
- Iteriere vom ersten Feld an bis wir auf das kleinste Feld stoßen, von dem wir weder wissen ob Hit oder Miss
- ermittle geschätze Mitte des Feldes und greife diese ID an