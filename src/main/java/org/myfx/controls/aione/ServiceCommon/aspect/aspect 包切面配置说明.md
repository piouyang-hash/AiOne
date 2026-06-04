# ====================== Aspect切面说明文档 ======================
## 1、各切面 @Order 配置明细
【类名 : Order数值】
AdminAuditAspect         : 100
ApiTimeRecordAspect      : 0
JwtCheckAspect           : 30
RequestContextAspect        : 10
RoleAuthAspect           : 60
ServiceAuthAspect        : 50
UserIdRateLimitAspect    : 40

## 2、Spring AOP 执行优先级（Order越小，越先执行，从上→下为执行先后顺序）
Spring规则：@Order值越小，切面越早切入目标方法
1. ApiTimeRecordAspect        (Order=0)    【最先执行：接口耗时统计，全局最前置切面】
2. RequestContextAspect         (Order=10)   【初始化请求上下文：IP/请求头/方法名，必须在JWT之前】
3. JwtCheckAspect            (Order=30)   【JWT鉴权，初始化User用户上下文】
4. UserIdRateLimitAspect     (Order=40)   【基于用户ID接口限流】
5. ServiceAuthAspect         (Order=50)   【服务权限校验】
6. RoleAuthAspect            (Order=60)   【角色权限校验】
7. AdminAuditAspect          (Order=100)  【最后执行：操作审计日志，优先级最低】

## 3、关键备注
1. RequestContextAspect(10) < JwtCheckAspect(30)：保证JWT切面可正常从RequestContext获取Token、请求信息；
2. Jwt鉴权成功后填充UserContext，后续限流、权限类切面均可读取登录用户信息；
3. @Around环绕逻辑：Order小的切面先进@Around前置逻辑、后置逻辑最后执行。
4. 现在不需要清除，使用的是声明式的ScopeValue，替代了ThreadLocal