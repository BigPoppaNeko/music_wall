# Sinestesia: cuando el sonido tiene color

Nikolai Rimsky-Korsakov veía Mi mayor como azul safiro. Alexander Scriabin veía Re mayor como amarillo, Fa mayor como rojo, La mayor como verde. Franz Liszt confundía a sus músicos de orquesta gritando "¡más azul, por favor!" o "¡esto necesita más rosa!". Duke Ellington veía los sonidos como colores: el do de Paul Gonsalves era plateado, las notas altas de Johnny Hodges eran caramelo.

La sinestesia es una condición neurológica donde la estimulación de una vía sensorial activa otra de forma involuntaria y consistente. En la variante más estudiada —la llamada cromestesia o sinestesia sonido-color— los sonidos generan experiencias visuales: colores, formas, texturas, incluso movimiento. No es metafórica. Las neuroimágenes muestran actividad real en la corteza visual cuando una persona sinesteta escucha música.

Lo que resulta extraordinario es que esta condición, siendo individual y no transmisible, generó uno de los debates más productivos en la historia del arte occidental: **¿existe una correspondencia natural entre el sonido y el color, o es puramente arbitraria y aprendida?**

---

## El proyecto de Scriabin

Alexander Scriabin era sinesteta y compositor, y decidió hacer de su condición una teoría estética. Para su sinfonía *Prometeo, el Poema del Fuego* (1910) diseñó una parte para "clavier à lumières" —un instrumento que proyectaba colores en la sala mientras sonaba la música, siguiendo sus propias correspondencias personales. La sinfonía está escrita en el manuscrito con una línea de colores encima de la partitura: un mapa visual de lo que Scriabin veía mientras componía.

El problema es que el clavier à lumières nunca funcionó bien en vida de Scriabin y la mayoría de sus contemporáneos no compartían sus asociaciones cromáticas. Rimsky-Korsakov, también sinesteta, discutía con él: para Rimsky, Mi mayor era azul; para Scriabin, azul era Fa sostenido. Las correspondencias individuales de los sinestetas son consistentes internamente pero divergentes entre personas. Scriabin creyó que estaba descubriendo una ley universal; en realidad estaba describiendo su neurología particular.

---

## Kandinsky y la abstracción como sinestesia universalizada

Wassily Kandinsky, que también era sinesteta, tomó un camino diferente. En lugar de intentar demostrar correspondencias exactas, construyó una teoría estética más sutil: que los colores y los sonidos comparten **propiedades emocionales** análogas, no identidades exactas.

En *De lo Espiritual en el Arte* (1911) Kandinsky articula estas correspondencias: el amarillo es agresivo y angustiante como una trompeta en forte; el azul es profundo y espiritual como un violonchelo; el verde es pasivo y satisfecho como el tono medio de un violín; el naranja es energético como una campana de iglesia; el blanco es un silencio pleno de posibilidades, no la ausencia de sonido sino la pausa.

Kandinsky no está hablando de físiología sino de **resonancia emocional compartida**. Y en ese sentido tiene razón: cuando se hace el experimento, la mayoría de personas asocian el amarillo con sonidos agudos y el azul con sonidos graves, el rojo con lo rápido y el lila con lo lento. Las correlaciones no son perfectas, pero son estadísticamente significativas. Existe un piso cultural y quizás evolutivo de correspondencias.

---

## La investigación contemporánea

Los estudios modernos han confirmado algunas de las intuiciones de Kandinsky. El experimento de Spence y Deroy (2012) mostró que el 90% de los participantes asociaban notas musicales agudas con colores claros y brillantes, y notas graves con colores oscuros. La velocidad musical (tempo) correlaciona con la saturación del color: música rápida = colores más saturados.

El trabajo de Palmer et al. (2013) con sinestetas y no-sinestetas mostró que, aunque los sinestetas tienen correspondencias más específicas e individuales, los no-sinestetas también tienen correspondencias estadísticamente predecibles. La sinestesia parece ser el extremo de un espectro que todos habitamos en diferente grado.

Más específicamente para el diseño visual:

- **Timbre**: los sonidos ricos en armónicos se asocian con texturas visuales más complejas. Un violín y un clarinete tocando la misma nota generan asociaciones visuales distintas.
- **Modo mayor/menor**: mayor → colores más saturados, más brillantes. Menor → colores más desaturados, más oscuros.
- **Tempo**: BPM bajo → paletas de baja saturación, colores fríos. BPM alto → paletas saturadas, colores cálidos.
- **Registro**: grave → colores oscuros, pesados. Agudo → colores claros, livianos.

---

## La emoción como mediador

La teoría más convincente para explicar por qué estas correspondencias existen es la mediación emocional: tanto los sonidos como los colores producen estados emocionales, y es esa capa emocional la que se comparte entre sentidos. Un bajo profundo y el color azul oscuro no se parecen físicamente, pero ambos generan una sensación de peso, de gravedad, de introversión. Un sonido agudo y el amarillo brillante ambos generan alertness, urgencia, extraversión.

Esto tiene una implicación directa para cualquier sistema que quiera hacer visible la música: **el camino no va de nota a color sino de emoción a color**. Si puedes inferir el perfil emocional de la escucha de un usuario —no qué notas escucha sino qué estados genera esa música— puedes usar esos estados para navegar el espacio del color.

---

## Lo que esto significa para MusicWall

Los datos de Last.fm no incluyen información musical directa (tonalidad, tempo, modo). Pero servicios como la API de Spotify o AcousticBrainz sí tienen `valence`, `energy`, `danceability` y `tempo` por canción. Esas variables son proxies de los estados emocionales que la música genera.

Una ruta posible, que mencionaré como potencial función antes de implementar: cruzar el Top 50 de canciones de un usuario con datos de valence/energy de Spotify → generar un "perfil emocional" → mapear ese perfil a una paleta cromática basada en las correspondencias de Kandinsky/Palmer. El resultado sería un fondo de color que no viene de las portadas sino de cómo se siente la música de ese usuario.

Eso es lo que hace un sinesteta de forma involuntaria. MusicWall podría hacerlo de forma programática.
