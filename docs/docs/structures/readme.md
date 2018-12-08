# מבנים

## הגדרה
הגדרת מבנה תעשה בפורמט הבא:
```
struct StructName(
  variablePropName: prop1Type,
  val constantPropName: prop2Type,
  ...
)
```
## יצירת עצם
יצירת עצמים ממבנה שהגדרנו תעשה כך:
```
val obj: StructName = StructName(prop1Value, prop2Value)
```

## שימוש
שימוש בעצם שנוצר, כלומר, הגישה לתכונותיו יעשו כך:
```
obj.propName
```
