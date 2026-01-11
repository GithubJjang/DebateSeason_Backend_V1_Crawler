# 타임리프.

## 1. 폴더 위치
resources/templates/에 위치한다.

## 2. Data 가져오는 방법.

```txt
msg는 model.attribute("msg",object)를 통해서 가져온 데이터이다.
<p th:text="${msg}">기본 텍스트</p> <- 단순 html 원문은 무시.

th: 이라고 태그를 붙이는 이유는 바로, 동적으로 적용하기 위해서이다.
안하면 정적으로 인식해서 안먹힘.
```

## 3. 추가 팁
```js
<input type="radio" th:name="|opinion_${stat.index}|" value="긍정">

<h3 th:text="${entry.key}" th:name="|category_${stat.index}|">카테고리</h3>


// 1. container라는 object객체
const container = {}

 for( let i=0; i<size; i++){


            const opinion = document.querySelector(`input[name="opinion_${i}"]:checked`)

            const category = document.querySelector(`h3[name="category_${i}"]`)

            // 2. input 태그의 value 및 h3의 text 가져오는 방법.
            console.log(opinion.value + category.textContent)

            container[category] = opinion

            }


```