import { useState } from 'react'
import heroImg from './assets/hero.png'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import './App.css'
import LibraryFooter from './assets/componets/LibraryFooter'
import LibraryBooks from './assets/componets/LibraryBooks'
import LibraryHeader from './assets/componets/LibraryHeader'
import LibraryButton from './assets/componets/LibraryButton'
import LibraryLogin from './assets/componets/LibraryLogin'
import LibraryCards from './assets/componets/LibraryCards'
import Office from './assets/componets/Office'

function App() {


  return (
    <div>
  {/*<LibraryHeader />
    <main>
        <h2>
          List Of Books
        </h2>
     <LibraryBooks />
    <LibraryButton />
         
    </main>
     <LibraryFooter />
*/}

     
    {/*<LibraryLogin/>*/}
    <Office/>
     
    </div>
    

    
  )
}

export default App
