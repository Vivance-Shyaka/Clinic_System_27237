import React, { useState } from 'react'
import LibraryButton from './LibraryButton'

function LibraryLogin() {
  const [doYouHaveAnAccount, setDoYouHaveAnAccount] = useState(true);
  const [Userame, setUserName] = useState("");
  const [Password, setPassword] = useState("");

    function LoginAction(){
        alert("You have successfully logged in: " +UserName+ " and your password is: " +Password+ "");
    }


  return (
    <>
    { doYouHaveAnAccount ? (
                    <div style={{border:"3px solid blue", marginTop:"190px"}}>
                        <label>UserName:</label>
                        <input type="text" onchange={e=> setUserName(e.target.value)} placeholder='Enter Your username'/><br/>
                        <label>Password:</label>
                        <input type="password" onchange={e=> setPassword(e.target.value)}/><br/>
                        <LibraryButton buttonLabel="LOGIN" onClickButton={LoginAction}/> <br/>
                        <label>New here? </label> <LibraryButton buttonLabel="Create Account"/>
                    </div>
    ):(
            <div style={{border:"3px solid indigo", marginTop:"190px"}}>
                <label>Student ID:</label>
                <input type="text" placeholder='Enter Your Student ID'/><br/>
                <label>Email:</label>
                <input type="email" /><br/>
                <label>Password:</label>
                <input type="password" /><br/>
                <label>Confirm Password:</label>
                <input type="password" /><br/>
                <LibraryButton buttonLabel="LOGIN"/> <br/>
                 <label>Already have an account?? </label> <LibraryButton buttonLabel="Sign In"/>
            </div>
    )
     }
    </>
  )
}

export default LibraryLogin