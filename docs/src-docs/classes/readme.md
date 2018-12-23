# מחלקות

## הגדרה
הגדרת **מחלקה** תעשה בפורמט הבא:
```
class ClassName {
  var variablePropName: Prop1Type
  val constantPropName: Prop2Type
  
  constructor(variablePropName: Prop1Type, constantPropName: Prop2Type) {
    this.variablePropName = variablePropName
	this.constantPropName = constantPropName
  }
  
  fun methodName(param1: Param1Type): ReturnType {
    // ...
	return
  }
}
```
## יצירת עצם
יצירת עצמים ממחלקה שהגדרנו תעשה כך:
```
val obj: ClassName = ClassName(variablePropValue, constantPropValue)
```

## שימוש
שימוש בעצם שנוצר, כלומר, הגישה לתכונות והשיטות שלו, יעשו כך:
```
obj.propName
obj.methodName(param1Value)
```
