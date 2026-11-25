import React from 'react'
import LibraryCards from './LibraryCards'

function LibraryBooks() {

    const books =[
        {
            code:"WEB 001",
            name:"Full stack Development",
            price: 100
            
        },
              {
            code:"ENG 001",
            name:"Software development",
            price: 90
            
        },
              {
            code:"WEB 002",
            name:"Software Engineering",
            price: 50
            
        }
    ]
  return (
    <>
    <div>
          <ul>
            {
       books.map(book =>(<LibraryCards bookInfo={book} />))
       }

 
      </ul>
    </div>

     <div>
      The total price for {books.length} books is: {books.reduce((total, book) => total + book.price, 0)}
      </div>  

  </>
  )
}

export default LibraryBooks
