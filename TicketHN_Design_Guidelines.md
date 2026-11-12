# TicketHN — Guía de identidad visual y lenguaje de diseño

**Versión:** 1.0 — Dirección creativa inicial  
**Producto:** TicketHN (Android nativo)  
**Concepto elegido:** **Portal** — Descubre experiencias. Accede con confianza.

> Este documento establece las bases de la identidad visual y de la experiencia del comprador. Es una guía de dirección de diseño, no un sistema de componentes cerrado ni un manual de marca definitivo. Las decisiones deben validarse en prototipos, con usuarios y en dispositivos Android reales.

---

## 1. Visión de producto

TicketHN es una plataforma hondureña para **descubrir eventos, comprar entradas digitales y acceder mediante códigos QR**. Debe transmitir dos sensaciones complementarias:

1. **Emoción antes de la compra:** descubrir conciertos, festivales, deportes, teatro y experiencias.
2. **Tranquilidad durante y después de la compra:** entender precios, completar el pago y encontrar rápidamente un boleto válido.

La marca no debe parecer exclusivamente musical, ni confundirse con una aplicación bancaria. Debe tener personalidad propia sin sacrificar la claridad de un producto comercial.

### Promesa de experiencia

**De descubrir un evento a tener tu entrada, sin complicaciones.**

### Personalidad

- **Moderna**, sin depender de modas pasajeras.
- **Confiable**, especialmente en pagos y validación.
- **Cercana**, con lenguaje natural para usuarios en Honduras.
- **Dinámica**, dando protagonismo a la experiencia del evento.
- **Clara**, con decisiones, precios y estados inequívocos.

---

## 2. Filosofía visual: «Portal»

### Idea central

Un **portal** conecta a la persona con la experiencia que quiere vivir. El acto de **descubrir un lugar** y el de **entrar a un evento** se convierten en un único símbolo y una narrativa coherente.

### Construcción conceptual del símbolo

El logotipo elegido combina:

- **Pin de ubicación:** exploración, eventos cercanos y presencia en Honduras.
- **Puerta abierta:** acceso, entrada digital, movimiento hacia una experiencia.
- **Espacio negativo:** apertura y simplicidad.

El símbolo debe mantenerse **minimalista, reconocible en tamaños pequeños y funcional sin el nombre de la marca**. El espacio interior de la puerta es un *hueco real*: **debe ser transparente**, no una pieza pintada de blanco. Asimismo, el exterior del símbolo, en las versiones sin fondo, debe permanecer transparente.

### Reglas del logo

1. Mantener las proporciones y el gesto de la puerta; no reinterpretar la figura con adornos.
2. No añadir sombras pesadas, reflejos, texturas, biseles ni efectos 3D.
3. Nunca cerrar o rellenar el hueco interior de la puerta con blanco: debe dejar ver el fondo donde se coloque.
4. Crear versiones optimizadas para fondos claros y oscuros, además de una variante monocromática.
5. Conservar suficiente espacio libre alrededor del símbolo para que respire.
6. No incorporar texto al ícono launcher; reservar el nombre **TicketHN** para el lockup y las superficies donde se requiera.
7. Antes de publicar, disponer de un maestro vectorial (SVG o equivalente) para obtener bordes limpios a cualquier tamaño.

**Nota sobre Android:** los íconos adaptables tienen capas y una zona de seguridad; no basta con exportar un cuadrado con esquinas redondeadas. Conviene preparar un *foreground* vectorial o PNG transparente, un *background* independiente y una variante monocromática para íconos temáticos. La pantalla de inicio del sistema Android 12+ también debe construirse conforme a las reglas del sistema, sin forzar una imagen de splash a pantalla completa.

---

## 3. Paleta cromática

Esta es la **paleta de referencia seleccionada**. Los valores son tokens iniciales y podrán ajustarse ligeramente al validar contraste y resultados de impresión/pantalla.

| Token | Nombre | HEX | Uso sugerido |
|---|---|---|---|
| `brand.primary` | Azul cobalto | `#2563EB` | Marca, enlaces, acciones principales, selección |
| `brand.navy` | Azul medianoche | `#0B1F4D` | Texto de alto énfasis, fondos oscuros, ícono dark |
| `brand.accent` | Verde esmeralda | `#10B981` | Acentos medidos y señalización positiva |
| `neutral.white` | Blanco | `#FFFFFF` | Superficies y fondos claros |

### Colores de interfaz derivados (propuesta inicial)

| Token | Modo claro | Modo oscuro | Uso |
|---|---|---|---|
| `surface.canvas` | `#F7F9FC` | `#091429` | Fondo general |
| `surface.card` | `#FFFFFF` | `#11213B` | Tarjetas y paneles |
| `text.primary` | `#0B1F4D` | `#F5F8FF` | Títulos y contenido prioritario |
| `text.secondary` | `#52627A` | `#B5C3D8` | Descripciones y metadatos |
| `border.subtle` | `#DCE4EF` | `#2B3B55` | Divisores y contornos |
| `action.primary` | `#2563EB` | `#6B96FF` | Botones primarios |
| `action.onPrimary` | `#FFFFFF` | `#07162B` | Texto sobre botones primarios |

Estos colores derivados **no sustituyen una auditoría de accesibilidad**: comprobar cada combinación real de texto/fondo y cada estado interactivo. En especial, el verde de marca no debe utilizarse automáticamente como texto sobre blanco ni asumirse que transmite por sí mismo el estado de éxito.

### Distribución del color

- **Predominio de neutros** para mantener la interfaz tranquila y legible.
- **Azul** como principal color de marca y de interacción.
- **Verde** como acento excepcional: detalles de la identidad, confirmaciones o hitos positivos. No inundar la interfaz de verde.
- **Fotografías y afiches** como principal fuente de diversidad y emoción visual.

**Evitar:** gradientes omnipresentes, tarjetas saturadas con el azul principal, fondos de colores alternados sin significado y mezclar varios colores de énfasis en una misma pantalla.

---

## 4. Tipografía

### Familia recomendada: Inter

Inter ofrece legibilidad sólida en pantallas pequeñas, números claros y una estética moderna sin resultar extravagante. Si se utiliza otra familia, debe mantener estas cualidades.

| Rol | Peso inicial | Uso |
|---|---|---|
| Display / titular promocional | 700 | Mensajes cortos sobre campañas y eventos destacados |
| Título de pantalla | 600–700 | Jerarquía de navegación |
| Título de tarjeta | 600 | Nombre del evento |
| Cuerpo | 400 | Descripciones e instrucciones |
| Etiquetas / controles | 500–600 | Botones, tabs, categorías |
| Metadatos | 400–500 | Fecha, ubicación, disponibilidad |
| Precio / total | 600–700 | Información económica crítica |

**Principios:**

- Usar tamaños y escalas basados en `sp` para respetar la configuración de accesibilidad de Android.
- Limitar la cantidad de tamaños y pesos; la jerarquía debe percibirse sin depender solo del color.
- Reservar negritas para información relevante, no para párrafos enteros.
- Mostrar precios con formato local consistente, por ejemplo **L 750.00**, y siempre indicar costos adicionales antes de confirmar el pago.
- No incrustar textos informativos importantes dentro de imágenes promocionales.

---

## 5. Lenguaje de interfaz

La interfaz se inspira en **Material Design 3**, pero la personalidad la aportan la marca, la fotografía, la composición y la tipografía.

### Geometría y espaciado

- Preferir superficies limpias y bordes de radio moderado.
- Propuesta de escala espacial: **4, 8, 12, 16, 24 y 32 dp**.
- Margen lateral orientativo: **16–20 dp** en teléfonos; ampliar en tabletas o pantallas grandes.
- Radio de tarjetas: **12–16 dp**; botones: **12–16 dp** o formas Material 3 cuando aporten consistencia.
- Usar elevación de manera excepcional; preferir separación, superficies y bordes sutiles.
- No comprimir el contenido para mostrar más eventos a costa de la lectura.

### Iconografía

- Íconos geométricos simples y coherentes, alineados con Material Symbols o una familia equivalente.
- Mismo grosor óptico y sistema de tamaños.
- Usar etiquetas cuando un ícono por sí solo pueda ser ambiguo.
- No usar QR como decoración repetitiva; reservarlo para boletos y funciones donde tenga sentido.

### Fotografía y afiches

**Los eventos son los protagonistas.** La identidad de TicketHN debe enmarcarlos, no competir con ellos.

- Mantener proporciones consistentes para imágenes dentro de cada tipo de tarjeta.
- Mostrar afiches oficiales cuando estén disponibles; usar recortes legibles y evitar cortar información esencial.
- En destacados, priorizar fotografías de calidad y una superposición moderada para leer título y CTA.
- Los afiches pueden tener muchos colores: las tarjetas y la navegación alrededor deben mantenerse sobrias.
- Proporcionar imágenes de respaldo y estados de carga para evitar saltos de disposición.

### Animación

Animaciones **breves, funcionales y discretas**: transiciones de navegación, carga de resultados, selección de localidad y confirmación de compra. Deben comunicar continuidad y estado, no retrasar tareas. Respetar la configuración del sistema para reducir movimiento.

---

## 6. Componentes base

### Botones

- **Primario:** una acción principal por contexto (por ejemplo, «Comprar boletos» o «Continuar al pago»).
- **Secundario:** acciones auxiliares sin competir con la principal.
- **Texto/terciario:** enlaces y ajustes de bajo énfasis.
- Incluir estados de normal, presionado, deshabilitado y cargando.
- Objetivo táctil recomendado de **al menos 48 × 48 dp**.

### Tarjeta de evento

Información prioritaria, en este orden aproximado:

1. Imagen o afiche del evento.
2. Nombre del evento.
3. Fecha y hora.
4. Ciudad / recinto.
5. Precio **desde** una cantidad, cuando corresponda.
6. Estado real de disponibilidad («Agotado», «Últimos disponibles») solo con datos respaldados por el sistema.

Evitar botones innecesarios dentro de cada tarjeta. Tocar la tarjeta abre el detalle; la compra se inicia desde una pantalla donde el usuario puede revisar precios y condiciones.

### Filtros y búsqueda

- Búsqueda visible en Explorar y fácil de alcanzar desde Inicio.
- Filtros por **ciudad, fecha y categoría**.
- Chips seleccionables con estado claramente visible; permitir eliminar filtros y ver resultados.
- No exigir registro para descubrir eventos.

### Localidades y cantidades

- Mostrar nombres claros: General, Preferencia, VIP, etc.
- Indicar precio por entrada, cantidad, disponibilidad y subtotal.
- Evitar colores como único identificador de una localidad.
- Mantener visible el resumen del total antes de avanzar.

### Estados del sistema

Cada pantalla debe contemplar: **cargando, vacío, éxito, error, sin conexión y contenido no disponible**. Los mensajes deben decir qué ocurrió y cuál es la siguiente acción posible. Nunca presentar un pago como exitoso sin confirmación verificable.

---

## 7. Filosofía UX: descubrir → elegir → comprar → entrar

### Flujo ideal del comprador

1. **Descubrir:** explorar eventos sin barreras.
2. **Entender:** ver rápidamente fecha, ubicación, localidades, costos y restricciones.
3. **Elegir:** seleccionar localidad y cantidad sin pasos innecesarios.
4. **Comprar:** presentar un resumen transparente; solicitar autenticación solo cuando sea necesaria.
5. **Acceder:** encontrar el boleto y su QR con claridad.

### Principios no negociables

- **Pocos pasos, sin sacrificar información:** reducir fricción, no ocultar detalles relevantes.
- **Precio final claro:** subtotal, cargos y total antes de autorizar el pago.
- **Estado del boleto evidente:** válido, usado, cancelado o pendiente, según reglas reales del backend.
- **Acceso prioritario a «Mis boletos»:** especialmente durante la llegada al evento.
- **Confianza por diseño:** nunca depender solo de candados decorativos o frases como «100% seguro». Usar información real y comprobable.
- **Errores recuperables:** conservar la selección de localidades cuando una operación falla y sea técnicamente posible.

### Navegación propuesta

Barra inferior del comprador con cuatro destinos:

**Inicio · Explorar · Mis boletos · Perfil**

Los flujos de compra se presentan con una jerarquía enfocada y posibilidad clara de regresar, sin multiplicar pantallas por motivos puramente visuales.

El acceso de organizadores y validadores será **dependiente de permisos**. No debe complicar la navegación predeterminada de quien solo compra entradas.

---

## 8. Dirección para la pantalla de Inicio

### Composición sugerida

1. **Cabecera ligera:** ciudad seleccionada y acceso a notificaciones.
2. **Búsqueda:** «Busca conciertos, deportes y más».
3. **Evento destacado:** foto protagonista, nombre, fecha y llamada a explorar o ver detalle.
4. **Categorías:** Música, Deportes, Teatro, Festivales, Otros.
5. **Próximos eventos:** tarjetas simples, ordenadas por relevancia o fecha.
6. **Descubrimiento contextual:** por ciudad o por fechas cercanas, solo si aporta valor.
7. **Navegación inferior:** cuatro destinos permanentes.

### Lo que debe sentirse al abrir TicketHN

«Aquí puedo encontrar qué hacer y comprar mi entrada con facilidad». La primera pantalla debe ayudar a decidir, no parecer un catálogo de promociones, un panel administrativo ni una infografía.

---

## 9. Modo claro y modo oscuro

### Claro

- Fondo suave y tarjetas blancas.
- Azul medianoche en títulos y azul cobalto en acciones.
- Verde exclusivamente como acento complementario.
- Fotografías con suficiente protagonismo y contenedores discretos.

### Oscuro

- Fondo azul muy profundo, evitando negro puro como única opción de marca.
- Superficies ligeramente diferenciadas.
- Texto claro de alta legibilidad y primario azul adaptado para contraste.
- No invertir automáticamente el logo si se pierde su lectura: utilizar la variante específica para dark mode.

En ambos modos, los códigos QR deben generarse con **alto contraste, margen de silencio suficiente y pruebas de lectura en dispositivos reales**. No aplicar filtros visuales que impidan el escaneo.

---

## 10. Accesibilidad y calidad

- Comprobar contraste de texto normal **mínimo 4.5:1** y de texto grande **mínimo 3:1**, salvo excepciones normativas aplicables.
- Asegurar áreas táctiles de al menos **48 × 48 dp**.
- Respetar tamaños de fuente del sistema, lectores de pantalla y navegación accesible.
- Combinar icono, texto y estado: no depender únicamente del color.
- Usar descripciones accesibles para imágenes e íconos relevantes; omitir descripciones redundantes para elementos decorativos.
- Probar casos con nombres de eventos largos, precios grandes, fechas extensas, conectividad deficiente y pantallas pequeñas.
- Verificar las adaptaciones de Android, incluida la barra de navegación, gestos, bordes y modo de pantalla completa cuando aplique.

---

## 11. Voz y microcopy

**Tono:** español natural, directo, cordial y profesional. Usar palabras familiares para usuarios hondureños, sin exagerar coloquialismos.

| Situación | Ejemplo recomendado |
|---|---|
| Búsqueda | «Busca eventos, artistas o lugares» |
| Acción principal | «Comprar boletos» |
| Localidad | «Selecciona tu localidad» |
| Resumen | «Total a pagar» |
| Compra confirmada | «¡Tu compra fue confirmada!» |
| Boletos | «Ver mis boletos» |
| Error recuperable | «No pudimos completar la operación. Inténtalo de nuevo.» |
| Evento agotado | «Boletos agotados» |

Evitar frases grandilocuentes, urgencia artificial y mensajes técnicos crudos del backend.

---

## 12. Tokens y aplicación técnica inicial

Para implementar el sistema en **Jetpack Compose**, centralizar en el tema de la app:

- `ColorScheme` para modo claro/oscuro.
- `Typography` basada en Inter o en una fuente de reserva coherente.
- `Shapes` con radios estandarizados.
- Espaciados reutilizables (`Spacing` o escala equivalente).
- Estados de componentes y variantes (primario, secundario, destructive, loading).

No introducir colores HEX directamente en decenas de composables. Los valores de marca deben mantenerse semánticamente separados de los colores de estado (`success`, `warning`, `error`) y de las superficies del tema.

### Recursos de marca recomendados

```text
brand/
  logo-primary.svg           # Maestro del símbolo / lockup
  logo-monochrome.svg        # Símbolo monocromático
  logo-light.png             # Exportación transparente para fondo claro
  logo-dark.png              # Exportación transparente para fondo oscuro
  launcher-foreground.png    # Símbolo aislado con transparencia
  launcher-monochrome.xml    # Capa monocromática temática (si se implementa)
```

Los nombres anteriores representan una **estructura sugerida**, no archivos necesariamente incluidos con este documento.

---

## 13. Cosas que TicketHN no debe ser

- Una plantilla genérica de e-commerce con tarjetas repetidas sin jerarquía.
- Una interfaz «neón de conciertos» que excluya deportes o teatro.
- Una app bancaria fría y excesivamente formal.
- Un diseño que ponga ilustraciones o el logo por encima de afiches y datos del evento.
- Un flujo que esconda el total, la localidad o la disponibilidad.
- Un catálogo lleno de sombras, gradientes y decoraciones que distraen.

---

## 14. Próximas decisiones de diseño

**Fase de sistema visual**

1. Confirmar versión vectorial final del símbolo y variantes claro/oscuro/monocromático.
2. Probar launcher adaptativo y splash de Android en distintos tamaños y máscaras.
3. Ajustar tokens cromáticos con pruebas de contraste reales.
4. Definir componentes reutilizables (botones, inputs, chips, tarjetas, estados).

**Fase de experiencia del comprador**

1. Diseñar Home y Explorar.
2. Diseñar detalle del evento y selección de localidades.
3. Diseñar checkout, confirmación y Mis boletos.
4. Diseñar QR, registro, acceso y perfil.
5. Probar el recorrido completo, incluyendo errores y estados vacíos.

---

## 15. Resumen ejecutivo

TicketHN utiliza la dirección creativa **Portal** para representar el paso de **descubrir un evento** a **entrar en él**. Su marca combina una **ubicación** con una **puerta abierta**; su interfaz usa **azul cobalto, azul medianoche, verde esmeralda y neutros claros**, con **Inter** como tipografía base.

El lenguaje visual es **minimalista, editorial y comercial**: deja que las fotografías despierten el interés, mientras que la navegación, los precios, la compra y el boleto digital permanecen claros y confiables. La meta no es impresionar por cantidad de elementos, sino hacer que **cada interacción se sienta evidente, rápida y segura**.
