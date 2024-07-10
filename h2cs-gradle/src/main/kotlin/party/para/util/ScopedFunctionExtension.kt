package party.para.util

fun <T, R> T?.takeIfNotNull(action: (T) -> R): R? {
    val t = this;
    if (t != null) {
        return action(t)
    }
    return null
}
