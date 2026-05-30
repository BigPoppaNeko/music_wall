# La paleta cromática de los géneros musicales

La sinestesia —la condición neurológica donde un estímulo sensorial activa involuntariamente otro— afecta a menos del 5% de la población. Pero existe una versión cultural de la sinestesia que afecta a casi todos: la asociación aprendida entre géneros musicales y colores. Es tan consistente que puedes testearla: muéstrale a cualquier persona una paleta de negro puro, rojo sangre y ocre y pregúntale qué música suena ahí. La respuesta, con una consistencia estadística notable, será metal.

Esto no es accidental. Durante décadas, las industrias discográfica y de diseño gráfico construyeron estos vocabularios cromáticos de forma colectiva, reforzándolos a través de miles de portadas, posters, camisetas y ambientaciones de conciertos hasta que se volvieron inseparables del sonido mismo. El color no describe el género: **lo convoca**.

A continuación, un análisis por género con sus paletas características y los mecanism os que las produjeron.

---

## Metal y sus subcategorías

**Heavy clásico** (Sabbath, Priest, Maiden): negro, rojo sangre `#8B0000`, naranja quemado `#CC5500`, amarillo dorado `#FFD700`. La paleta del fuego y la oscuridad. Eddie, la mascota de Iron Maiden, es literalmente naranja sobre negro desde 1980.

**Death metal**: negro `#0A0A0A`, verde putrefacción `#4A5B2A`, marrón tierra `#3D2B1F`, rojo oscuro `#5C0000`. Los logos son deliberadamente ilegibles, casi jeroglíficos. El color de lo que se descompone.

**Black metal**: blanco roto `#E8E8E0`, gris plomo `#708090`, negro azulado `#0D1117`. La paleta del frío, la nieve, los bosques nórdicos a las 3 AM. Las fotos en blanco y negro con "corpse paint" convirtieron la escala de grises en la paleta definitiva del género.

**Doom**: morado oscuro `#2D1B69`, gris piedra `#7B7B8B`, amarillo azufre `#D4AF37`. Las portadas del doom clásico tienen una deuda visual con la pintura prerrafaelita: cuerpos femeninos en estados de trance o muerte, cielos tormentosos, tonos que parecen haber absorbido demasiada melancolía.

---

## Jazz

La paleta del jazz es la del humo, el ámbar y la noche urbana: ámbar `#FFBF00`, azul marino `#003366`, crema `#FFFDD0`, negro profundo `#0A0A0A`. Blue Note estableció el negro y el blanco como vocabulario primario, pero el jazz vocal y el swing tienen los dorados y cremas de las luces de los clubes de los años 40.

El jazz fusión de los 70 —Herbie Hancock, Weather Report, Return to Forever— introdujo una paleta radicalmente distinta: naranjas ácidos, azules eléctricos, verdes fosforescentes. La influencia del arte psicodélico y el afrofuturismo producen portadas que se parecen más al rock progresivo que al bebop.

---

## Hip-hop

El hip-hop tiene quizás la mayor diversidad cromática de cualquier género, pero sus períodos son distinguibles.

**Golden age (1987–1998)**: colores de la calle, graffiti, fotografía de alto contraste. Verdes urbanos `#4A7C59`, azules denim `#1560BD`, dorado `#FFD700`. La portada de *Illmatic* es azul fría. *Ready to Die* es negro y blanco. *The Chronic* introduce el verde hierba `#228B22` —una marca cromática de G-funk tan reconocible como cualquier logo.

**Trap y hip-hop moderno**: negro `#000000`, blanco `#FFFFFF`, rojo `#FF0000`, dorado `#FFD700`. O bien: blanco puro, casi quirúrgico. La estética de Kanye con *My Beautiful Dark Twisted Fantasy* es completamente diferente —una pintura renacentista barroca— pero es la excepción que confirma la regla.

---

## Ambient y música electrónica contemplativa

Eno, Biosphere, Stars of the Lid, Grouper, William Basinski: gris perla `#D3D3D3`, azul niebla `#B0C4DE`, blanco roto `#F5F5F0`, verde grisáceo `#8FBC8F`. Son las paletas de la lluvia vista desde una ventana, del cielo antes del amanecer, del desierto después de varios días solo.

El ambient tiene miedo de los colores saturados. La saturación implica urgencia, claridad, presencia. El ambient es exactamente lo contrario.

---

## Shoegaze y dream pop

La paleta del ensueño fotoquímico: lavanda `#E6E6FA`, rosa deslavado `#FFB6C1`, blanco sobreexpuesto `#FFFFF0`, azul pálido `#ADD8E6`. Las portadas de My Bloody Valentine, Slowdive, Mazzy Star son fotografías que parecen haber sido sacadas por alguien que olvidó remover el filtro. El grano, el desenfoque y la sobreexposición son estéticos, no accidentales. *Loveless* de MBV es casi monocromática: rojo apagado sobre negro, como si el rojo también estuviera soñando.

---

## Post-rock y música instrumental épica

Sigur Rós, Mogwai, Explosions in the Sky, Godspeed You! Black Emperor: blanco glaciar `#F0F8FF`, gris marengo `#4A4A4A`, azul islandés `#4682B4`, negro absoluto `#000000`. Las portadas son frecuentemente fotografías de paisajes desolados: carreteras que terminan en nada, cielos en tormenta, ciudades vistas desde satélite. La música es épica pero melancólica; la paleta refleja eso con colores que son hermosos y fríos al mismo tiempo.

---

## Electrónica de baile (house, techno, EDM)

Dos tradiciones paralelas. La rave culture original del techno de Detroit y el house de Chicago usaba paletas industriales: negro, naranja `#FF6600`, verde neón `#39FF14`. El acid house tiene el amarillo de las caritas sonrientes `#FFFF00`.

La EDM comercial moderna tiene una paleta completamente diferente: gradientes de azul-púrpura-rosa, colores que parecen diseñados por un algoritmo de Instagram. Brillantes, saturados, eléctricos. No hay sombras. Todo es frente.

---

## Lo que esto significa para MusicWall

Esta cartografía cromática es **materia prima directa**. Si Last.fm sabe que un usuario escucha principalmente metal y ambient, la paleta del wallpaper debería poder reflejar esa tensión: el negro caliente del metal y el gris frío del ambient creando algo que no es ni uno ni otro pero que es *exactamente ese usuario*.

El análisis de portadas que ya hace el renderer extrae `dominantColor`, `brightness` y `saturation` por imagen. El siguiente paso —del que avisaré antes de implementar— sería cruzar esos datos con una tabla de correspondencias género-paleta para **modular** la generación de color en lugar de solo extraerla de las imágenes.

Eso permitiría algo que ningún renderer actual hace: un wallpaper que visualmente "suene" al género que domina la escucha del usuario, incluso cuando las portadas individuales no lo transmitan con fuerza.
