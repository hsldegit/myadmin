package cn.gan.web.sys.controller;

import cn.gan.web.sys.bean.Result;
import cn.gan.web.sys.bean.SysUser;
import cn.gan.web.sys.service.SysUnitService;
import cn.gan.web.sys.service.SysUserService;
import com.alibaba.fastjson.JSON;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUnitService sysUnitService;

    private Logger logger = LoggerFactory.getLogger(SysUserController.class);

    // 用户登录。
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result<String> login(@RequestBody Map<String,Object> reqMap, HttpServletRequest request){
        System.out.println(request.getRequestURI() + ":" + request.getSession().getId());
        String username = (String) reqMap.get("username"), password = (String) reqMap.get("password");
        logger.debug("user login username : {} , password : {}", username, password);
        SysUser sysUser = sysUserService.findByLoginName(username,false);
        Map<String, Object> data = new HashMap<String, Object>();
        logger.debug("user:{}", JSON.toJSONString(sysUser));
        if (sysUser == null){
            return Result.error("用户名或密码错误!");
        }
        try{
            SecurityUtils.getSubject().login(new UsernamePasswordToken(username,
                    new Sha256Hash(password, sysUser.getSalt()).toHex()));
        }catch (UnknownAccountException | IncorrectCredentialsException e){
            return Result.error("用户名或密码错误!");
        }
        request.getSession().setAttribute("me", sysUser.getId());
        return Result.success("登录成功!");
    }

    @RequiresUser
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Result<SysUser> userInfo(){
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        return Result.success(sysUser);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public Result<String> logout(HttpSession session){
        try {
            session.invalidate();
        }finally {
            return Result.success("退出成功!");
        }
    }

    // 查询所有用户。
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @RequiresUser
    public Result<List<SysUser>> data(){
        Subject subject = SecurityUtils.getSubject();
        // TODO 加上分页。
        List<SysUser> users = sysUserService.findAll();
        return Result.success(users);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @RequiresUser
    public Result<String> add(@RequestBody SysUser sysUser, HttpSession session){
        logger.debug("login user is : {}, create user : {}",session.getAttribute("me"), JSON.toJSONString(sysUser));
        if (sysUser.getUnitId() == null || !sysUnitService.isExistById(sysUser.getUnitId()))
            return Result.error("单位选择错误！");
        System.out.println(sysUser.getPassword());
        sysUser.setOpBy((String) session.getAttribute("me"));
        sysUserService.addUser(sysUser);
        return Result.success("成功!");
    }

}
