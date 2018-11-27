package movies.flag.pt.moviesapp.utils;

public class ServerResponse<ResponseEntity> {

    private ResponseEntity responseEntity;
    private int errorType;

    public ServerResponse(ResponseEntity responseEntity, int errorType){
        this.responseEntity = responseEntity;
        this.errorType = errorType;
    }

    public int getErrorType() {
        return errorType;
    }

    public ResponseEntity getResponseEntity() {
        return responseEntity;
    }
}
