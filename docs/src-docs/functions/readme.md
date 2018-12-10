# פונקציות
הגדרת **פונקציה** תעשה בפורמט הבא:
```
fun functionName: ReturnType = { p1: p1Type, p2: p2Type, ... ->
  // Some actions...
  // The last expression in this block is the returned value of the function.
}
```
שם הפונקציה יכול להכיל כל אות אנגלית, קווים תחתונים, ואופרטורים מהקבוצה: `{+-/*%&|#}`.

## פונקציות גנריות
ניתן להגדיר **פונקציות גנריות**, המקבלות ופועלות על סוג דינמי של נתונים כך:
```
fun <T> genericFunction: ReturnType = { p1: T, p2: AnotherType, ... ->
  // function body
}
```

## בלוק
הגדרת **בלוק** תעשה בפורמט הבא:
```
{ ->
  // Some actions...
  // The last expression in this block is the returned value of the function.
  // The return type is inferred by the type of the last expression.
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
**פוקנציית הרחבה** היא פונקצייה המקושרת ל**מבנה** כלשהו.  
הפונקצייה צריכה לקבל לפחות פרמטר אחד.
ניתן להגדיר כזו ולהשתמש בה בפורמט הבא:
```
fun StructName.functionName: ReturnType = { param1: ParamType, ... -> 
  // in this block (and his children),
  // "this" refers to the object (in our case, obj) that the function applied on it.
}

val obj = StructName()
obj.functionName(param1, ...)
```
