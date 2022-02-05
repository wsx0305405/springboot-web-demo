## 員工管理系統

以Springboot的方式簡單的呈現資料CRUD的操作



使用實體類模擬資料庫的資料  所以每一次執行時資料會被重置



#### 模擬database

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Department {
    private Integer id;
    private String departmentName;
}
```

```java
@Data
@NoArgsConstructor
public class Employee {
    private Integer id;
    private String lastName;
    private String email;
    private Integer gender;
    private Department department;
    private Date birth;

    public Employee(Integer id, String lastName, String email, Integer gender, Department department) {
        this.id = id;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.department = department;
        // 預設創建日期
        this.birth = new Date();
    }
}
```

#### Dao層

- DepartmentDao

```java
@Repository
public class DepartmentDao {
    // 模擬資料庫中的資料

    private static Map<Integer, Department> departments = null;

    static {
        departments = new HashMap<Integer,Department>(); // 創建一個部門表

        departments.put(101,new Department(101,"AAA部門"));
        departments.put(102,new Department(102,"BBB部門"));
        departments.put(103,new Department(103,"CCC部門"));
        departments.put(104,new Department(104,"DDD部門"));
        departments.put(105,new Department(105,"EEE部門"));
    }

    // 獲得所有部門訊息
    public Collection<Department> getDepartments(){
        return departments.values();
    }

    // 通過id得到部門
    public Department getDepartmentById(Integer id){
        return departments.get(id);
    }


}
```

- EmployeeDao

```java
@Repository
public class EmployeeDao {
    // 模擬資料庫中的資料  員工ID + 員工訊息
    private static Map<Integer, Employee> employees = null;
    // 員工有所屬的部門
    @Autowired
    private DepartmentDao departmentDao;

    static {
        employees = new HashMap<Integer, Employee>(); // 創建一個部門表

        employees.put(1001,new Employee(1001,"AA","AAA@com",1,new Department(101,"AAA部門")));
        employees.put(1002,new Employee(1002,"BB","BBB@com",0,new Department(102,"BBB部門")));
        employees.put(1003,new Employee(1003,"CC","CCC@com",1,new Department(103,"CCC部門")));
        employees.put(1004,new Employee(1004,"DD","DDD@com",0,new Department(104,"DDD部門")));
        employees.put(1005,new Employee(1005,"EE","EEE@com",1,new Department(105,"EEE部門")));
    }
    // 主鍵自增
    private static Integer initId = 1006;
    // 增加一個員工
    public void save(Employee employee){
        if(employee.getId()==null){
            employee.setId(initId++);
        }
        // 由 employee 得到對應的部門
        Department department = employee.getDepartment();
        // 再由部門得到對應的部門id
        Integer id = department.getId();
        // 由得到的部門id 執行Dao層 藉由id得到部門
        Department departmentById = departmentDao.getDepartmentById(id);
        // 設置傳入的employee的部門
        employee.setDepartment(departmentById);
        // 將設定完成的employee 放入employees中
        employees.put(employee.getId(),employee);

    }

    // 查詢全部員工訊息
    public Collection<Employee> getAll(){

        return employees.values();
    }

    // 通過id查詢員工
    public Employee getEmployeeById(Integer id){
        return employees.get(id);

    }

    // 刪除員工 通過id
    public void delete(Integer id){
        employees.remove(id);
    }


}
```





#### 登入功能

- 首頁配置 : 所有頁面的靜態資源都需要使用thymeleaf接管; @{}

- 登入頁面 表單
  - 根據要輸入的值標記上對應的 name 屬性

```html
<form th:action="@{/user/login}">
    <h1 class=""> sign in </h1>
    <p style="color: crimson" th:text="${msg}"></p>
    <label class="">Username</label>
    <input type="text" name="username" class="" placeholder="Username" required><br>
    <label class="">Password</label>
    <input type="password" name="password" class="" placeholder="Password" required>
    <div class="">
        <label>
            <input type="checkbox" value="remerber-me"> Remember me
        </label>
    </div>
    <button class="" type="submit"> Sign in </button>
</form>
```



- LoginContrller
  - @RequestParam 接收前端傳入的name進入方法中判斷
  - 設定只要username不為null 且 password 是 "123456" 即可登入

```java
@Controller
public class LoginController {

    @RequestMapping("/user/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Model model){
        // 具體的業務
        if(!StringUtils.isEmpty(username) && "123456".equals(password)){
            // 設置session值
            session.setAttribute("loginUser",username);
            return "redirect:/main.html";
        }else{
            // 登入失敗 提示登入失敗
            model.addAttribute("msg","用戶名或是密碼錯誤");
            return "index";
        }
    }
}
```



- MvcConfig
  - 根據傳入的地址回傳實際的頁面位址

```java
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {
    // 跳轉視圖
    @Override
    public void addViewControllers(ViewControllerRegistry registry){
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/index.html").setViewName("index");
        registry.addViewController("/main.html").setViewName("dashboard");
    }
}
```



#### 攔截功能

- 攔截 config
  - 判斷session存在與否
    - 不存在 表示沒有登入 返回未登入
    - 存在則繼續執行

```java
public class LoginHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 登入成功後 會有用戶的Session
        Object loginUser = request.getSession().getAttribute("loginUser");
        if(loginUser == null){ // 未登入
            request.setAttribute("msg","沒有權限 請先登入");
            request.getRequestDispatcher("/index.html").forward(request,response);
            return false;
        }else {
            return true;
        }
    }

}
```

- 實作攔截config
  - 使用 addInterceptors 增加攔截方法
  - 將之前定義的 InterceptorRegistry 加入攔截中
  - addPathPatterns 增加攔截路徑
  - excludePathPatterns 增加例外的攔截路徑

```java
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginHandlerInterceptor()).addPathPatterns("/**")
                .excludePathPatterns("/index.html","/","/user/login");
    }
}
```



#### 展示員工列表

- Controller視圖跳轉
  - 跳轉至employee資料夾下的list.html檔案
  - 顯示所有員工列表 得到所有的員工 getAll()
  - 將得到的結果放入Attribute

```java
@Controller
public class EmployeeController {

    @Autowired
    EmployeeDao employeeDao;

    @RequestMapping("/emps")
    public String list(Model model){
        Collection<Employee> employees = employeeDao.getAll();
        model.addAttribute("emps",employees);
        return "employee/list";
    }
}
```

- 列表循環展示
  - 將得到的結果 emps 遍歷

```html
<body>
<main role="main" class="col-md-9 ml-sm-auto clo-lg-10 pt-3 px-4">
    <div class="table-responsive">
        <table class="table table-striped table-sm">
            <thead>
            <tr>
                <th>id</th>
                <th>lastName</th>
                <th>email</th>
                <th>gender</th>
                <th>department</th>
                <th>birth</th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="emp:${emps}"><br>
                <td th:text="${emp.getId()}"></td>
                <td>[[${emp.getLastName()}]]</td>
                <td th:text="${emp.getEmail()}"></td>
                <td th:text="${emp.getGender()==0?'女':'男'}"></td>
                <td th:text="${emp.department.getDepartmentName()}"></td>
                <td th:text="${#dates.format(emp.getBirth(),'yyyy-MM-dd HH:mm:ss')}"></td>
                <td>
                    <button class="btn btn-sm btm-primery">編輯</button>
                    <button class="btn btn-sm btm-danger">刪除</button>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</main>
</body>
```



#### 增加員工實現

- 按鈕提交
- 跳轉到添加頁面
  - 在顯示頁面中增加跳轉的按鈕

```html
<a class="btn btn-sm btn-success" th:href="@{/emp}"> 添加員工 </a>
```

對應的Get方法中跳轉到添加頁面

```java
@GetMapping("/emp")
public String toAddpage(Model model){
    // 查出所有部門的訊息
    Collection<Department> departments = departmentDao.getDepartments();
    model.addAttribute("departments",departments);
    return "employee/add";
}
```

設計添加表單

- 以Post的方式傳送到 /emp

```html
<body>
    <form th:action="@{/emp}" method="post">
        <div class="form-group">
            <label>LastName</label>
            <input type="text" name="lastName" class="form-control" placeholder="">
        </div>
        <div class="form-group">
            <label>Email</label>
            <input type="email" name="email" class="form-control" placeholder="">
        </div>
        <div class="form-group">
            <label>Gender</label>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="gender" value="1">
                <label class="form-check-label">男</label>
            </div>
            <div>
                <input class="form-check-input" type="radio" name="gender" value="1">
                <label class="form-check-label">女</label>
            </div>
        </div>
        <div class="form-group">
            <label>department</label>
            <select class="form-control" name="department.id">
                <option th:each="dep:${departments}" th:text="${dep.getDepartmentName()}" th:value="${dep.getId()}"></option>
            </select>
        </div>
        <div class="form-group">
            <label>Birth</label>
            <input type="text" class="form-control" name="birth" placeholder="">
        </div>
        <button type="submit" class="btn btn-primary">添加</button>
    </form>
</body>
```

接收POST請求

- 調用底層的業務方法 添加操作

```java
@PostMapping("/emp")
public String addEmp(Employee employee){
    // 添加的操作
    employeeDao.save(employee); // 調用底層業務方法 保存員工訊息

    return "redirect:emps";
}
```



#### 修改員工

- list頁面中增加修改的跳轉路徑
  - 跳轉時 攜帶目前員工的Id

```html
<a class="btn btn-sm btm-primery" th:href="@{/emp/{id}(id=${emp.getId()})}">編輯</a>
```

- 將對應的路徑寫入Controller執行
  - 由員工的id得到對應的員工
  - 設置Atrribute傳遞到前端頁面 emp , departments
  - 跳轉至update頁面

```java
// 員工修改頁面
@GetMapping ("/emp/{id}")
public String toUpdateEmp(@PathVariable("id")Integer id,Model model){
    // 查出原來的數據
    Employee employee = employeeDao.getEmployeeById(id);
    model.addAttribute("emp",employee);
    // 查出所有部門的訊息
    Collection<Department> departments = departmentDao.getDepartments();
    model.addAttribute("departments",departments);

    return "employee/update";
}
```

- update
  - 由傳遞到前端的參數 emp  departments 進行value的賦值
  - 最終傳向 < form th:action="@{/updateEmp}" method="post" > 提交表單

```html
<body>
<form th:action="@{/updateEmp}" method="post">
    <input type="hidden" name="id" th:value="${emp.getId()}">
    <div class="form-group">
        <label>LastName</label>
        <input type="text" th:value="${emp.getLastName()}" name="lastName" class="form-control" placeholder="ABC">
    </div>
    <div class="form-group">
        <label>Email</label>
        <input type="email" th:value="${emp.getEmail()}" name="email" class="form-control" placeholder="ABC@com">
    </div>
    <div class="form-group">
        <label>Gender</label>
        <div class="form-check form-check-inline">
            <input th:checked="${emp.getGender() == 1}" class="form-check-input" type="radio" name="gender" value="1">
            <label class="form-check-label">男</label>
        </div>
        <div class="form-check form-check-inline">
            <input th:checked="${emp.getGender() == 0}" class="form-check-input" type="radio" name="gender" value="0">
            <label class="form-check-label">女</label>
        </div>
    </div>
    <div class="form-group">
        <label>department</label>
        <select class="form-control" name="department.id">
            <option th:each="dep:${departments}" th:text="${dep.getDepartmentName()}"
                    th:value="${dep.getId()}" th:selected="${dep.getId()==emp.getDepartment().getId()}">
            </option>
        </select>
    </div>
    <div class="form-group">
        <label>Birth</label>
        <input th:value="${#dates.format(emp.getBirth(),'yyyy-MM-dd')}" type="text" name="birth" class="form-control" placeholder="">
    </div>
    <button type="submit" class="btn btn-primary">修改</button>
</form>

</body>
```

- 對應的Controller  /updateEmp
  - 將表單修改過的內容封裝成employee傳遞
  - 執行 save 方法將資料儲存
  - 重定向回list頁面  顯示所有資料

```java
@PostMapping("/updateEmp")
public String updateEmp(Employee employee){
    System.out.println("update emp = " + employee);
    employeeDao.save(employee);
    return "redirect:/emps";
}
```



#### 刪除員工

- 在list中新增刪除路徑
  - 傳遞時攜帶對應員工的id

```html
<a class="btn btn-sm btm-danger" th:href="@{/delemp/{id}(id=${emp.getId()})}">刪除</a>
```

- 對應的Controller
  - 執行Dao層的delete方法刪除
  - 最後重定向回list頁面

```java
@GetMapping("/delemp/{id}")
public String deleteEmp(@PathVariable("id")int id){
    employeeDao.delete(id);
    return "redirect:/emps";
}
```



