# Composición y jerarquía: qué hace que una imagen mande

Hay imágenes que se miran una vez y se olvidan. Hay imágenes que se miran una vez y se recuerdan treinta años. La diferencia no es el tema, no el estilo, no la calidad técnica de la ejecución. La diferencia es la composición: la decisión deliberada de dónde va qué y por qué.

La composición es el estudio de cómo el ojo se mueve por una imagen y qué encuentra en cada estación de ese recorrido. Un compositor visual no controla lo que piensa el espectador pero sí controla la secuencia en la que percibe los elementos. Esa secuencia produce una experiencia. Diseñar la secuencia es diseñar la experiencia.

---

## La jerarquía visual: qué se ve primero

El ojo humano no escanea las imágenes aleatoriamente. Tiene preferencias evolutivas y culturales que determinan el orden de atención. En orden de prioridad:

**Contraste**: el punto de mayor contraste en una imagen es donde el ojo va primero. Un punto blanco sobre fondo negro se ve antes que cualquier otra cosa. Esto explica por qué los diseñadores de interfaces colocan el botón de acción principal en el color con mayor contraste respecto al fondo.

**Tamaño**: los elementos más grandes tienen más peso visual y atraen la mirada antes que los elementos pequeños. Cuando hay un conflicto entre el contraste y el tamaño —un elemento pequeño muy contrastado vs. un elemento grande moderadamente contrastado— el contraste generalmente gana.

**Posición**: en culturas con escritura de izquierda a derecha, el ojo tiende a iniciar el escaneo en la esquina superior izquierda y terminar en la inferior derecha. Los elementos en las posiciones del "inicio del recorrido" tienen ventaja perceptual. Pero la posición central también tiene un atractivo especial: el centro geométrico es donde el ojo descansa cuando no hay otras señales que lo dirijan.

**Color**: los colores cálidos (rojo, naranja, amarillo) avanzan perceptualmente —parecen estar más cerca— mientras que los colores fríos (azul, verde, violeta) retroceden. Un elemento rojo sobre fondo azul se percibirá como más prominente que el mismo elemento azul sobre fondo rojo, incluso si sus tamaños son idénticos.

**Movimiento implícito**: las diagonales se perciben como más activas que las horizontales o verticales. Una imagen que contiene diagonales fuertes tiene energía visual que las composiciones horizontales no tienen. Los ángulos agudos producen tensión; los ángulos obtusos, calma.

---

## La regla de los tercios y por qué funciona

La regla de los tercios —dividir el espacio en una cuadrícula de 3×3 y colocar los elementos importantes en las intersecciones— es probablemente la regla compositiva más citada y más malentendida del diseño visual.

Funciona porque las intersecciones de los tercios coinciden aproximadamente con los puntos donde el ojo se detiene durante el escaneo natural de una imagen. No son los únicos puntos, pero son estadísticamente frecuentes. Colocar el elemento principal en una de esas intersecciones garantiza que el ojo lo encuentre en su recorrido natural.

Pero la regla de los tercios no es una garantía de éxito: es un punto de partida. Las mejores composiciones frecuentemente la violan deliberadamente para crear tensión (elementos muy centrados = presencia, poder), movimiento (elementos en los bordes = dinamismo, salida del cuadro) o sorpresa (elementos en posiciones inusuales que obligan al ojo a hacer un recorrido inesperado).

---

## La sección áurea y la espiral de Fibonacci

La razón áurea (φ ≈ 1.618) aparece en la naturaleza con una frecuencia que llevó a los griegos a considerarla divina: en la distribución de semillas en un girasol, en la espiral de un nautilus, en la ramificación de los árboles. La espiral logarítmica que genera esa razón —la espiral de Fibonacci— tiene la propiedad de ser autosimilar: cualquier sección de la espiral tiene la misma forma que el todo.

El EcosystemRenderer ya usa el ángulo áureo (137.508°) para distribuir las portadas en el campo de color, siguiendo exactamente la misma lógica por la que las semillas de girasol se distribuyen de esa forma: es la distribución que minimiza el solapamiento en un espacio circular con crecimiento continuo. El resultado es una distribución que parece orgánica aunque sea completamente matemática.

Esta conexión entre matemáticas y percepción de lo natural no es coincidencia. La naturaleza usó evolución para optimizar sus distribuciones; los humanos aprendemos a percibir esas distribuciones como "naturales" precisamente porque crecemos rodeados de ellas. Cuando un sistema generativo usa las mismas matemáticas que la naturaleza, el resultado parece natural aunque sea creado por código.

---

## Profundidad y capas: el z-axis implícito

Una imagen plana puede crear una ilusión de profundidad. Las técnicas para esto son antiguas —los pintores del Renacimiento las teorizaron exhaustivamente— y funcionan en el diseño visual contemporáneo exactamente igual:

**Solapamiento**: cuando un elemento cubre parcialmente otro, el que cubre parece estar en primer plano. La portada del Mosaic Renderer usa esto de forma literal: las portadas se superponen y el ojo crea automáticamente un espacio tridimensional.

**Tamaño decremental**: los elementos más lejanos se perciben como más pequeños. Si en la composición los elementos menores se colocan en la parte superior y los mayores en la inferior (simulando una vista en perspectiva desde el suelo), el cerebro construye un espacio.

**Desenfoque**: los elementos en foco parecen más cercanos que los desenfocados. El EcosystemRenderer usa técnicas análogas con sus capas de blur progresivo: la capa de aura está más difuminada que la de haze, que está más difuminada que la ghost.

**Contraste decremental**: en la atmósfera real, los elementos distantes tienen menos contraste y más azul (dispersión de Rayleigh). Simular eso artificialmente produce una ilusión de profundidad incluso sin perspectiva geométrica.

---

## Tensión, equilibrio y asimetría deliberada

Las composiciones simétricas transmiten estabilidad, permanencia, autoridad. Los monumentos son simétricos. Las instituciones son simétricas. Los altares son simétricos. La simetría dice: esto es sólido, esto dura, esto no se cuestiona.

Las composiciones asimétricas transmiten dinamismo, búsqueda, proceso. Están en movimiento, o contienen una tensión que el ojo intenta resolver. La asimetría bien ejecutada es más interesante que la simetría bien ejecutada, aunque sea más difícil de lograr.

El secreto de la asimetría compositiva es el **equilibrio visual sin simetría**. Se puede equilibrar una imagen usando peso visual (un elemento grande a la izquierda equilibrado por múltiples elementos pequeños a la derecha), contraste (un elemento muy contrastado equilibrado por una zona de color plano) o tensión (dos elementos en posiciones que crean un eje diagonal de energía).

Las mejores portadas de disco son composiciones asimétricas con equilibrio perfecto. *The Dark Side of the Moon* tiene el prisma ligeramente off-center y el haz de luz que lo cruza creando un eje diagonal que lleva el ojo de una esquina a la otra. *Unknown Pleasures* de Joy Division (1979) tiene las líneas de pulsar centradas verticalmente pero con asimetría vertical que las hace parecer en movimiento.

---

## Lo que esto significa para MusicWall

De todos los ensayos de esta colección, este es quizás el más directamente operacional para los renderers.

El **HeroRenderer** usa jerarquía por tamaño correctamente, pero podría mejorarse con posicionamiento en tercios (el hero ligeramente arriba del centro en lugar de centrado) y con gradación de contraste (las portadas satélite ligeramente menos contrastadas que el hero para crear profundidad).

El **MuseumRenderer** usa simetría compositiva (filas centradas) que es correcta para la estética de galería pero podría ser más interesante con asimetría intencional: por ejemplo, la portada más escuchada ligeramente más grande que las demás, rompiendo la uniformidad del grid.

El **ConstellationRenderer** ya tiene equilibrio asimétrico por el ángulo áureo, pero las conexiones entre portadas son actualmente solo lineales (rank-adyacente). Conexiones que creen triángulos o formas geométricas más complejas añadirían tensión compositiva.

El **EcosystemRenderer** tiene la composición más sofisticada de todos: capas, profundidad, flujo, geometría sagrada. El problema es que toda esa sofisticación puede aplanar la jerarquía: cuando todo tiene el mismo nivel de detalle, el ojo no sabe dónde descansar. Necesita un punto focal claro —un elemento que mande inequívocamente— del que el resto derive su significado.

La composición no es decoración. Es la arquitectura invisible que determina si una imagen se olvida o se recuerda.
