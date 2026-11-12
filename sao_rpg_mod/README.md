# SAO RPG (Fabric 1.21.11)

Mod base de RPG inspirado en Sword Art Online + el sistema de equipamiento libre de Albion Online.

## Qué incluye

**Niveles (1-100)**
- Ganas XP matando mobs hostiles. Cada mob tiene nivel (según distancia al centro del mundo (0,0) y dimensión) y se muestra en su nombre: `[Lv.12] Zombie`.
- Matar mobs de nivel mucho más bajo que el tuyo da poca XP; de nivel más alto, más.
- Cada nivel: +1 de vida máxima y +0.1 de ataque. Subir de nivel cura y recarga energía.
- Los mobs también escalan: +10% vida y +4% daño por nivel.

**HUD estilo SAO** (tecla `H` para alternar con los corazones normales)
- Nombre, nivel, barra de vida (verde → amarilla → roja), barra de energía, barra de XP con `XP actual / necesaria (faltan N)`.
- Muestra la rama del objeto en tu mano y si te falta maestría (`req. 18`).
- La preferencia se guarda en `config/sao_rpg_client.properties`.

**Energía**: correr gasta, atacar cuesta 3, y se regenera sola. Sin energía no puedes atacar cuerpo a cuerpo ni correr.

**Clases libres (estilo Albion)**
No hay clase fija. Cada rama de equipo tiene su propia maestría (0-100), que sube al usarla:
`Espada, Hacha, Lanza, Arco, Ballesta, Tridente, Maza, Armadura ligera / media / pesada`.

| Material | Maestría requerida |
|---|---|
| Madera, cuero, tortuga, arco | 0 |
| Piedra, oro, ballesta | 3 |
| Cobre | 4 |
| Cota de malla | 5 |
| Hierro | 8 |
| Tridente | 12 |
| Diamante | 18 |
| Maza | 25 |
| Netherita | 30 |

Si usas algo sin la maestría necesaria **funciona pero peor** (arma: -4% de daño por nivel faltante, mínimo 35%; armadura: más daño recibido). Así siempre puedes entrenar la rama. Tecla `J` abre el tablero de maestrías.

**Comandos (OP)**: `/sao xp add <jugador> <n>`, `/sao level set <jugador> <n>`, `/sao mastery set <jugador> <rama> <n>`, `/sao reset <jugador>`

## Compilar
1. Instala JDK 21.
2. En la carpeta del proyecto: `gradle wrapper` (una vez) y luego `./gradlew build`.
3. El `.jar` queda en `build/libs/`. Ponlo en `mods/` junto con Fabric API.

También puedes abrir la carpeta con IntelliJ IDEA e importar como proyecto Gradle. Las versiones están en `gradle.properties`.

## Dónde ajustar el balance
Todas las fórmulas están en `logic/RpgMath.java`, los requisitos por material en `logic/GearRules.java`.

## Pendiente / siguientes pasos
- Rarezas de armas (común → legendaria) con stats propios.
- Inventario con estilo SAO (menú).
- Estilos de combate / skills de espada con energía.
- Mejores texturas del HUD (ahora se dibuja con rectángulos y degradados).
