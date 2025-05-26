import ch.wesr.slidebuilder.Slide

fun parseSlides(response: String): List<Slide> =
    response
        .lines()
        .filter { it.contains(":") }
        .map { line ->
            val (title, pointsRaw) = line.split(":", limit = 2)
            val bulletPoints = pointsRaw.split(";").map { it.trim() }.filter { it.isNotBlank() }
            Slide(title.trim(), bulletPoints)
        }
