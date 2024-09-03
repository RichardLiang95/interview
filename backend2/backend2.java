import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Role {
    int GUEST = 0;
    int USER = 1;
    int ADMIN = 2;

    int value() default GUEST;
}


@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果请求的处理器是方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;

            // 获取方法上的 Role 注解
            Role roleAnnotation = method.getMethodAnnotation(Role.class);

            // 如果方法上没有 Role 注解，允许访问
            if (roleAnnotation == null) {
                return true;
            }

            // 获取用户的角色，假设通过某种方式存储在会话中（如在登录时设置）
            Integer userRole = (Integer) request.getSession().getAttribute("userRole");
            if (userRole == null) {
                userRole = Role.GUEST; // 默认是游客
            }

            // 检查用户角色是否有权限访问
            if (userRole >= roleAnnotation.value()) {
                return true; // 有权限
            } else {
                // 无权限，返回 403 Forbidden
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
                return false;
            }
        }
        return true;
    }
}


@RestController
public class UserController {

    @Role(value = Role.GUEST)
    @PostMapping("/api/login")
    public ResponseEntity<Void> login(@RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        // 简单模拟用户身份验证
        if ("admin".equals(username) && "password".equals(password)) {
            // 登录为管理员
            request.getSession().setAttribute("userRole", Role.ADMIN);
        } else if ("user".equals(username) && "password".equals(password)) {
            // 登录为普通用户
            request.getSession().setAttribute("userRole", Role.USER);
        } else {
            // 登录失败，保持为游客
            request.getSession().setAttribute("userRole", Role.GUEST);
            return ResponseEntity.status(401).build(); // 返回 401 Unauthorized
        }
        return ResponseEntity.ok().build(); // 登录成功
    }

    @Role(value = Role.USER)
    @GetMapping("/api/user")
    public ResponseEntity<Void> getUser(@RequestParam String id) {
        return ResponseEntity.ok().build();
    }

    @Role(value = Role.ADMIN)
    @DeleteMapping("/api/user")
    public ResponseEntity<Void> deleteUser(@RequestParam String id) {
        return ResponseEntity.ok().build();
    }
}


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor).addPathPatterns("/api/**"); // 仅拦截 /api/ 下的请求
    }
}
