"use client"
import { usePathname } from 'next/navigation'
import { ReactNode } from 'react'
import { motion } from 'framer-motion'

export default function ContentArea({ children }: { children: ReactNode }) {
  const pathname = usePathname()

  return (
    <main className="flex-1 ml-64 bg-background min-h-screen">
      <motion.div
        key={pathname}
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -12 }}
        transition={{ duration: 0.3, ease: "easeOut" }}
        className="container py-8"
      >
        {children}
      </motion.div>
    </main>
  )
}
