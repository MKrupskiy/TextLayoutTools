# TextLayoutTools

### Краткое описание:

Аналог Punto Switcher для телефонов и планшетов на Android.

### Описание:
Смена раскладки УЖЕ ВВЕДЕННОГО текста, как это сделано в Punto Switcher для Windows.

Главный функционал - это автоматическая замена слов, набранных в другой раскладке.
Например, при вводе сообщения в чате: "Ружз, дружище!" программа заменит первое слово на "Hello".

Можно переключать раскладку текста несколькими способами:
1. Выделить текст, выбрать в меню действий "NTRCN -> ТЕКСТ".
2. Выделить текст, нажать комбинацию Ctrl+Q (меняется в настройках).
3. Выделить текст, нажать комбинацию Alt+Enter. При этом изменится и язык клавиатуры.
4. Установить курсор в пределах слова и нажать Ctrl+Q.
5. Текст меняется автоматически после пробела или знака препинания. Если программа ошиблась - достаточно нажать Ctrl+Q, и слово вернётся в исходное состояние.

Можно назначить свою комбинацию (по умолчанию Right_Shift+Q).

Для работы необходимо включить сервис "Text Layout Tools" (ссылка на меню, где это можно сделать, есть на главной - "Открыть Специальные Возможности")


Приложение мониторит ввод для отслеживания комбинации и замены текста.
Введенный текст остается только в приложении, ни на какие сервера не отправляется. При анализе текста для определения языка используются локальные словари. Если сомневаетесь, не включайте сервис.


### Эмуляция комбинаций Ctrl+A/C/V/X
Для заграничных Key2 добавлена эмуляция комбинаций Ctrl+A/C/V/X.
Включается в настройках приложения и работает с клавишей Speed Key. Для правильной работы необходимо назначить на эти кнопки вызов пустого приложения - DummyApp, иначе будет выскакивать предложение назначить ярлык на кнопку.


### Индикация текущего языка ввода
Можно включить отображение текущего языка ввода в шторке уведомлений.


### Плавающий индикатор языка
Можно включить отображение текущего языка ввода на экране.
Перетаскивается по экрану, есть настройки стиля.
Для работы необходимо дать разрешение на отображение поверх других окон.


### Пользовательский словарь
Если какое-то слово ошибочно (не)переводится в нужную раскладку, нужно выделить слово и в контекстном меню нажать "Добавить в словарь TLT".
Также, после 3-го исправления слова по Ctrl+Q программа добавит его в словарь автоматически.
