# בקרת זרימה

## משפטי תנאי
**משפט תנאי** מורכב מתנאי כלשהו (משתנה מסוג `Bool`) ורצף פעולות שירוצו במידה והערך הוא `true`.
```
val x = 3
if (x > 2) {
  println("x is greater than 2.")
}
```
ניתן לצרף משפטי תנאי רצף פעולו שירוץ במידה והתנאי לא מתקיים באמצעות המילה `else`.
```
val x = 1
if (x > 2) {
  println("x is greater than 2.")
} else {
  println("x is less than 2.")
}
```
ניתן להשתמש במשפטי תנאי בהגדרת ערך.
```
val stringToPrint = if (x > 2) "x is greater than 2." else "x is less than 2."
println(stringToPrint)
```

## משפטי when
ניתן לקצר  לולאת תנאים ארוכה למשפט when בודד.
```
val x = 1
val y = 3
val stringToPrint = when {
  x > 2 -> "x is greater than 2"
  y < 4 -> "x is'nt greater than 2 and y is less than 3"
  else -> "x is'nt greater than 2 and y is'nt less than 3"
}
println(stringToPrint)
```

## לולאות
ניתן להגדיר קטע קוד מסויים שירוץ כל עוד ערכו של תנאי (משתנה מסוג `Bool`) הוא `true`.
```
var x: Int = 0
while (x < 10) {
  x = x + 1
  println(x)
}
```