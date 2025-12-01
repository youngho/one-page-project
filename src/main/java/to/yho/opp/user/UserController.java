package to.yho.opp.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping(value = "/user")
    public ResponseEntity<UserVo> postCode(@RequestBody UserVo vo) {
        return new ResponseEntity<>(service.retrieve(vo), HttpStatus.OK);
    }
}
