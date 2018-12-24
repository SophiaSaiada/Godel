# פונקציות
הגדרת **פונקציה** תעשה בפורמט הבא:
```
fun functionName(p1: p1Type, p2: p2Type): ReturnType {
  // Some actions...
  return
}
```
שם הפונקציה יכול להכיל כל אות אנגלית, ספרות וקווים תחתונים.


ניתן להגדיר פונקציות (שאינן גנריות, פונקציות הרחבה או שיטות מחלקה) גם בצורה הבאה:
```
val functionName = { p1: p1Type, p2: p2Type ->
  // the last expression in function body is the returned value
}
```
## פונקציות גנריות
ניתן להגדיר **פונקציות גנריות**, המקבלות ופועלות על סוג דינמי של נתונים כך:
```
fun functionName<T>(genericParam: T, p2: p2Type): ReturnType {
  // Some actions...
  return
}
```


## הרצה
הרצת פונקציה תעשה בפורמט הבא: `(... ,functionName(p1, p2`.
כאשר הפרמטר האחרון של פונקציה הוא בלוק, ניתן להריצה גם בפורמט הבא:
```
functionName(p1) {
  // block body
}
```

## פונקציות הרחבה
**פוקנציית הרחבה** היא פונקצייה המתייחסת ל**מחלקה** כלשהי.  
הפונקצייה צריכה לקבל לפחות פרמטר אחד.
ניתן להגדיר כזו ולהשתמש בה בפורמט הבא:
```
fun ClassName.functionName(param1: ParamType): ReturnType {
  // in this block (and his children),
  // "this" refers to the object (in our case, obj) that the function applied on it.
}

val obj = ClassName()
obj.functionName(param1)
```
